package scraping;

import static utils.ArrayJS.*;
import static utils.Nyu.*;

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
    String code;
  }

  class Subjects {
    final Map<String, SchedgeV1Subject> subjects;

    @JsonCreator
    Subjects(final Map<String, SchedgeV1Subject> subjects) {
      this.subjects = subjects;
    }
  }

  class Schools {
    final Map<String, String> schools;

    @JsonCreator
    Schools(final Map<String, String> schools) {
      this.schools = schools;
    }
  }

  public static List<Course> scrapeFromSchedge(Term term) {
    var subjects = Subject.allSubjects().listIterator();
    var client = HttpClient.newHttpClient();
    var termString = term.json();

    var schools = run(() -> {
      var schoolsUri = URI.create(SCHEDGE_URL + termString);
      var request = HttpRequest.newBuilder().uri(schoolsUri).GET().build();
      var handler = HttpResponse.BodyHandlers.ofString();
      var resp = tcPass(() -> client.send(request, handler));
      // var subjects = SCHEDGE_URL();
      schools = fromJson(data, Schools.class);
    });

    Subjects subjectsA = null;

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
