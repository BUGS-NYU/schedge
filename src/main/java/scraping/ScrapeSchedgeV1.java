package scraping;

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

  public static List<Course> scrapeFromSchedge(Term term) {
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

    var subjectsFullCodeList = new ArrayList<String>();
    subjectsFullCodeList.ensureCapacity(subjectsList.size());
    for (var subject : subjectsList) {
      subjectsFullCodeList.add(subject.fullCode);
    }
    var subjects = subjectsFullCodeList.listIterator();

    var engine = new FutureEngine<String>();
    for (int i = 0; i < 20; i++) {
      if (subjects.hasNext()) {
        engine.add(getData(client, term, subjects.next()));
      }
    }

    var output = new ArrayList<Course>();
    for (String text : engine) {
      if (subjects.hasNext()) {
        engine.add(getData(client, term, subjects.next()));
      }

      if (text == null) {
        continue;
      }

      var courses = JsonMapper.fromJson(text, Course[].class);
      output.ensureCapacity(output.size() + courses.length);
      for (var course : courses) {
        output.add(course);
      }
    }

    return output;
  }

  private static Future<String> getData(HttpClient client, Term term,
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

      String text = resp.body();
      return text;
    });
  }
}
