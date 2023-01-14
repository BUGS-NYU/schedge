package scraping;

import static scraping.ScrapeSchedgeV2.*;
import static utils.ArrayJS.*;
import static utils.JsonMapper.*;
import static utils.Nyu.*;
import static utils.Try.*;

import com.fasterxml.jackson.annotation.*;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import org.slf4j.*;
import utils.*;

public final class ScrapeSchedgeV1 implements TermScrapeResult {
  static Logger logger = LoggerFactory.getLogger("scraping.ScrapeSchedgeV1");

  private static final String SCHEDGE_URL = "https://schedge.a1liu.com/";
  private static final HashMap<String, String> missingPrograms;

  static {
    var programs = new HashMap<String, String>();

    programs.put("NT", "Non-Credit Tisch School of the Arts");
    programs.put("GH", "NYU Abu Dhabi - Graduate");
    programs.put("CD", "College of Dentistry Continuing Education");
    programs.put("DN", "College of Dentistry - Graduate");

    // @TODO: what the hell are these - below are educated guesses
    programs.put("ND", "Non-Credit College of Dentistry");
    programs.put("NY", "Non-Credit Tandon School of Engineering");
    programs.put("NB", "Non-Credit Leonard N. Stern School of Business");
    programs.put("NE", "Non-Credit Steinhardt School of Culture, Education, and Human Development");
    programs.put("NH", "Non-Credit NYU Abu Dhabi");
    programs.put("NI", "Non-Credit NYU Shanghai");

    missingPrograms = programs;
  }

  static class NameField {
    // Object comes in the form: {"name":"name here"}
    public String name;
  }

  static class SchedgeV1Subject {
    String name;
    String fullCode;

    String schoolCode;
    String subjectCode;
  }

  static class Subjects {
    final ArrayList<SchedgeV1Subject> subjects;

    @JsonCreator
    Subjects(final Map<String, Map<String, NameField>> subjects) {
      this.subjects = new ArrayList<>();

      for (var schoolPair : subjects.entrySet()) {
        var schoolCode = schoolPair.getKey();
        var schoolSubjects = schoolPair.getValue();

        var size = this.subjects.size();
        this.subjects.ensureCapacity(size + schoolSubjects.size());

        for (var subjectPair : schoolSubjects.entrySet()) {
          var subject = new SchedgeV1Subject();
          subject.subjectCode = subjectPair.getKey();
          subject.name = subjectPair.getValue().name;
          subject.schoolCode = schoolCode;
          subject.fullCode = subject.subjectCode + '-' + subject.schoolCode;

          this.subjects.add(subject);
        }
      }
    }
  }

  static class Schools {
    final Map<String, String> schools;

    @JsonCreator
    Schools(final Map<String, NameField> schools) {
      this.schools = new HashMap<>();

      for (var pair : missingPrograms.entrySet()) {
        this.schools.put(pair.getKey(), pair.getValue());
      }

      for (var pair : schools.entrySet()) {
        var schoolCode = pair.getKey();
        var schoolName = pair.getValue().name;

        if (schoolName == null || schoolName.isEmpty()) {
          schoolName = missingPrograms.get(schoolCode);
        }

        if (schoolName == null) throw new RuntimeException("Code: " + schoolCode);

        this.schools.put(schoolCode, schoolName);
      }
    }
  }

  @JsonIgnoreProperties(
      value = {"subjectCode"},
      allowGetters = true)
  static class SchedgeV1Course {
    public String name;
    public String deptCourseId;
    public String description;
    public List<SchedgeV1Section> sections;
  }

  static class SchedgeV1Section {
    public int registrationNumber;
    public String code;
    public String name;
    public String description;
    public String[] instructors;
    public String type;
    public SectionStatus status;
    public List<Meeting> meetings;
    public List<SchedgeV1Section> recitations;
    public Integer waitlistTotal;
    public String instructionMode;
    public String campus;
    public Double minUnits;
    public Double maxUnits;
    public String grading;
    public String location;
    public String prerequisites;
    public String notes;
  }

  private final ArrayList<School> schools = new ArrayList<>();
  private final HttpClient client = HttpClient.newHttpClient();
  private final Iterator<String> subjects;
  private final FutureEngine<ScrapeResult> engine = new FutureEngine<>();
  private final Try ctx;
  private final Term term;
  private final Consumer<ScrapeEvent> consumer;

  private ScrapeSchedgeV1(Term term, Consumer<ScrapeEvent> consumer) {
    this.term = term;
    this.ctx = Try.Ctx(logger);
    this.consumer = consumer;

    consumer.accept(ScrapeEvent.message(null, "Fetching subject list..."));
    consumer.accept(ScrapeEvent.hintChange(-1));

    var schools =
        run(
            () -> {
              var schoolsUri = URI.create(SCHEDGE_URL + "schools");
              var request = HttpRequest.newBuilder().uri(schoolsUri).GET().build();
              var handler = HttpResponse.BodyHandlers.ofString();
              var resp = tcPass(() -> client.send(request, handler));
              var data = resp.body();

              return fromJson(data, Schools.class).schools;
            });

    var subjectsList =
        run(
            () -> {
              var schoolsUri = URI.create(SCHEDGE_URL + "subjects");
              var request = HttpRequest.newBuilder().uri(schoolsUri).GET().build();
              var handler = HttpResponse.BodyHandlers.ofString();
              var resp = tcPass(() -> client.send(request, handler));
              var data = resp.body();

              return fromJson(data, Subjects.class).subjects;
            });

    consumer.accept(ScrapeEvent.hintChange(subjectsList.size() + 1));
    consumer.accept(ScrapeEvent.progress(1));

    var schoolsMap = new HashMap<String, School>();
    var subjectsFullCodeList = new ArrayList<String>();
    subjectsFullCodeList.ensureCapacity(subjectsList.size());

    for (var subject : subjectsList) {
      subjectsFullCodeList.add(subject.fullCode);
      var school =
          schoolsMap.computeIfAbsent(
              subject.schoolCode,
              code -> {
                var name = schools.get(code);
                if (name == null) throw new RuntimeException("Code: " + code);

                var s = new School(name, code);
                this.schools.add(s);

                return s;
              });

      var s = new Subject(subject.fullCode, subject.name);
      school.subjects.add(s);
    }

    this.subjects = subjectsFullCodeList.listIterator();

    for (int i = 0; i < 10; i++) {
      if (subjects.hasNext()) {
        engine.add(getData(client, term, subjects.next()));
      }
    }
  }

  @Override
  public ArrayList<School> schools() {
    return schools;
  }

  @Override
  public boolean hasNext() {
    return this.engine.hasNext();
  }

  @Override
  public ArrayList<Course> next() {
    for (var result : engine) {
      if (subjects.hasNext()) {
        engine.add(getData(client, term, subjects.next()));
      }

      if (result.text == null) continue;

      consumer.accept(ScrapeEvent.message(result.subject, "Fetching " + result.subject));
      var courses = JsonMapper.fromJson(result.text, SchedgeV1Course[].class);
      var out = new ArrayList<Course>();

      for (var course : courses) {
        var c = new Course();
        c.name = course.name;
        c.deptCourseId = course.deptCourseId;
        c.description = course.description;
        c.subjectCode = result.subject;
        c.sections = new ArrayList<>();

        if (c.description == null) c.description = "";

        for (var section : course.sections) {
          var s = translateSection(section);
          if (!s.name.equals(c.name)) s.name = null;

          c.sections.add(s);
        }

        out.add(c);
      }

      consumer.accept(ScrapeEvent.progress(1));
      return out;
    }

    return null;
  }

  public static TermScrapeResult scrapeFromSchedge(Term term, Consumer<ScrapeEvent> consumer) {
    return new ScrapeSchedgeV1(term, consumer);
  }

  private static Section translateSection(SchedgeV1Section section) {
    var s = new Section();

    s.name = section.name;
    s.code = section.code;
    s.registrationNumber = section.registrationNumber;
    s.minUnits = section.minUnits;
    s.maxUnits = section.maxUnits;
    s.location = section.location;
    s.campus = section.campus;
    s.type = section.type;
    s.status = section.status;
    s.grading = section.grading;
    s.meetings = section.meetings;
    s.instructors = section.instructors;
    s.waitlistTotal = section.waitlistTotal;

    var mode = section.instructionMode;
    s.instructionMode = Objects.requireNonNullElse(mode, "In-Person");

    if (s.meetings == null) s.meetings = new ArrayList<>();

    if (section.prerequisites == null) section.prerequisites = "";
    if (section.notes == null) section.notes = "";

    section.prerequisites = section.prerequisites.trim();

    s.notes = section.notes + "\nPREREQUISITES: " + section.prerequisites;

    if (section.recitations != null) {
      s.recitations = new ArrayList<>();

      for (var recitation : section.recitations) {
        var r = translateSection(recitation);
        s.recitations.add(r);
      }
    }

    return s;
  }

  private static Future<ScrapeResult> getData(HttpClient client, Term term, String subject) {
    var parts = subject.split("-");
    String school = parts[1];
    String major = parts[0];

    if (school.contentEquals("UI")) {
      school = "SHU";
    }

    var components = new String[] {"" + term.year(), term.semester().toString(), school, major};

    var uri = URI.create(SCHEDGE_URL + String.join("/", components) + "?full=true");

    var request = HttpRequest.newBuilder().uri(uri).build();

    long start = System.nanoTime();

    var handler = HttpResponse.BodyHandlers.ofString();
    var fut = client.sendAsync(request, handler);
    return fut.handleAsync(
        (resp, throwable) -> {
          long end = System.nanoTime();
          double duration = (end - start) / 1000000000.0;
          logger.info("Fetching took {} seconds: subject={}", duration, subject);

          if (resp == null) {
            logger.error("Error (subject={}): {}", subject, throwable.getMessage());

            return null;
          }

          var result = new ScrapeResult();
          result.text = resp.body();
          result.subject = subject;

          return result;
        });
  }

  public Term term() {
    return term;
  }
}
