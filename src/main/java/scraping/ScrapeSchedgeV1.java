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
import org.slf4j.*;
import utils.*;

public final class ScrapeSchedgeV1 {
  static Logger logger = LoggerFactory.getLogger("scraping.ScrapeSchedgeV1");

  private static final String SCHEDGE_URL = "https://schedge.a1liu.com/";
  private static final HashMap<String, String> missingPrograms;

  static {
    var programs = new HashMap<String, String>();
    programs.put("NT", "Non-Credit Tisch School of the Arts");
    programs.put("GH", "NYU Abu Dhabi - Graduate");
    programs.put("CD", "College of Dentistry Continuing Education");
    programs.put("DN", "College of Dentistry - Graduate");

    missingPrograms = programs;
  }

  class SchedgeV1Subject {
    String name;
    String fullCode;

    String schoolCode;
    String subjectCode;
  }

  class Subjects {
    final ArrayList<SchedgeV1Subject> subjects;

    @JsonCreator
    Subjects(final Map<String, Map<String, String>> subjects) {
      this.subjects = new ArrayList<>();

      for (var schoolPair : subjects.entrySet()) {
        var schoolCode = schoolPair.getKey();
        var schoolSubjects = schoolPair.getValue();

        var size = this.subjects.size();
        this.subjects.ensureCapacity(size + schoolSubjects.size());

        for (var subjectPair : schoolSubjects.entrySet()) {
          var subject = new SchedgeV1Subject();
          subject.subjectCode = subjectPair.getKey();
          subject.name = subjectPair.getValue();
          subject.schoolCode = schoolCode;
          subject.fullCode = subject.subjectCode + '-' + subject.schoolCode;

          this.subjects.add(subject);
        }
      }
    }
  }

  class Schools {
    final Map<String, String> schools;

    @JsonCreator
    Schools(final Map<String, String> schools) {
      this.schools = schools;

      for (var pair : missingPrograms.entrySet()) {
        var schoolCode = pair.getKey();
        var foundName = schools.get(schoolCode);
        if (foundName == null || foundName.isEmpty()) {
          schools.put(schoolCode, pair.getValue());
        }
      }
    }
  }

  class SchedgeV1Course {
    public String name;
    public String deptCourseId;
    public String description;
    public List<SchedgeV1Section> sections;
  }

  class SchedgeV1Section {
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
    public String notes;
  }

  public static ScrapeTermResult scrapeFromSchedge(Term term) {
    var client = HttpClient.newHttpClient();
    var termString = term.json();

    var schools = run(() -> {
      var schoolsUri = URI.create(SCHEDGE_URL + "schools");
      var request = HttpRequest.newBuilder().uri(schoolsUri).GET().build();
      var handler = HttpResponse.BodyHandlers.ofString();
      var resp = tcPass(() -> client.send(request, handler));
      var data = resp.body();

      return fromJson(data, Schools.class).schools;
    });

    var subjectsList = run(() -> {
      var schoolsUri = URI.create(SCHEDGE_URL + "subjects");
      var request = HttpRequest.newBuilder().uri(schoolsUri).GET().build();
      var handler = HttpResponse.BodyHandlers.ofString();
      var resp = tcPass(() -> client.send(request, handler));
      var data = resp.body();

      return fromJson(data, Subjects.class).subjects;
    });

    var out = new ScrapeTermResult();
    out.schools = new ArrayList<>();

    var schoolsMap = new HashMap<String, School>();
    var subjectsFullCodeList = new ArrayList<String>();
    subjectsFullCodeList.ensureCapacity(subjectsList.size());

    for (var subject : subjectsList) {
      subjectsFullCodeList.add(subject.fullCode);
      var school = schoolsMap.computeIfAbsent(subject.schoolCode, code -> {
        var name = schools.get(code);
        var s = new School(name, code);
        out.schools.add(s);

        return s;
      });

      var s = new Subject(subject.fullCode, subject.name);
      school.subjects.add(s);
    }
    var subjects = subjectsFullCodeList.listIterator();

    var engine = new FutureEngine<ScrapeResult>();
    for (int i = 0; i < 20; i++) {
      if (subjects.hasNext()) {
        engine.add(getData(client, term, subjects.next()));
      }
    }

    for (var result : engine) {
      if (subjects.hasNext()) {
        engine.add(getData(client, term, subjects.next()));
      }

      if (result.text == null) {
        continue;
      }

      var courses = JsonMapper.fromJson(result.text, SchedgeV1Course[].class);
      var size = out.courses.size();
      out.courses.ensureCapacity(size + courses.length);

      for (var course : courses) {
        var c = new Course();
        c.name = course.name;
        c.deptCourseId = course.deptCourseId;
        c.description = course.description;
        c.subjectCode = result.subject;
        c.sections = new ArrayList<>();

        var descriptions = new HashMap<String, Integer>();

        for (var section : course.sections) {
          var s = new Section();

          descriptions.compute(section.name, (k, prev) -> {
            if (prev == null)
              prev = 0;

            return prev + 1;
          });

          if (!section.name.equals(c.name)) {
            s.name = section.name;
          }

          c.sections.add(s);
        }

        out.courses.add(c);
      }
    }

    return out;
  }

  private static Future<ScrapeResult> getData(HttpClient client, Term term,
                                              String subject) {
    var parts = subject.split("-");
    String school = parts[1];
    String major = parts[0];

    // @TODO Fix this hack to work around weird behavior from V1 and NYU
    if (school.contentEquals("UI")) {
      school = "SHU";
    }

    var components =
        new String[] {"" + term.year, term.semester.toString(), school, major};

    var uri =
        URI.create(SCHEDGE_URL + String.join("/", components) + "?full=true");

    var request = HttpRequest.newBuilder().uri(uri).build();

    long start = System.nanoTime();

    var handler = HttpResponse.BodyHandlers.ofString();
    var fut = client.sendAsync(request, handler);
    return fut.handleAsync((resp, throwable) -> {
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
}
