package scraping;

import static utils.JsonMapper.*;
import static utils.Nyu.*;
import static utils.Try.*;

import api.v1.*;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;
import org.slf4j.*;
import utils.*;

public final class ScrapeSchedgeV2 {
  private static Logger logger =
      LoggerFactory.getLogger("scraping.ScrapeSchedge2");

  private static final String LIST_SCHOOLS =
      "https://nyu.a1liu.com/api/schools/";
  private static final String COURSES = "https://nyu.a1liu.com/api/courses/";

  public static final class ScrapeTermResult {
    public ArrayList<School> schools;
    public ArrayList<Course> courses;
  }

  static final class ScrapeResult {
    String text;
    String subject;
  }

  public static ScrapeTermResult scrapeFromSchedge(Term term) {
    return scrapeFromSchedge(term, null);
  }

  public static ScrapeTermResult scrapeFromSchedge(Term term, List<String> inputSubjectList) {
    var client = HttpClient.newHttpClient();
    var termString = term.json();

    var schoolsUri = URI.create(LIST_SCHOOLS + termString);
    var request = HttpRequest.newBuilder().uri(schoolsUri).GET().build();
    var handler = HttpResponse.BodyHandlers.ofString();
    var resp = tcPass(() -> client.send(request, handler));
    var data = resp.body();

    var info = fromJson(data, SchoolInfoEndpoint.Info.class);

    var out = new ScrapeTermResult();
    out.schools = info.schools;

    var subjectList = new ArrayList<String>();

    for (var school : info.schools) {
      for (var subject : school.subjects) {
        subjectList.add(subject.code);
      }
    }

    if (inputSubjectList == null) {
      inputSubjectList = subjectList;
    }

    out.courses = scrapeFromSchedge(client, term, inputSubjectList);
    return out;
  }

  private static ArrayList<Course> scrapeFromSchedge(HttpClient client, Term term, List<String> subjectList) {
    var subjects = subjectList.iterator();
    var engine = new FutureEngine<ScrapeResult>();

    for (int i = 0; i < 20; i++) {
      if (subjects.hasNext()) {
        engine.add(getData(client, term, subjects.next()));
      }
    }

    var out = new ArrayList<Course>();
    for (var result : engine) {
      if (subjects.hasNext()) {
        engine.add(getData(client, term, subjects.next()));
      }

      if (result == null) {
        continue;
      }

      var courses = fromJson(result.text, Course[].class);
      out.ensureCapacity(out.size() + courses.length);
      for (var course : courses) {
        out.add(course);
      }
    }

    return out;
  }

  private static Future<ScrapeResult> getData(HttpClient client, Term term,
                                              String subject) {
    var uri = URI.create(COURSES + term.json() + "/" + subject);
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

      var out = new ScrapeResult();
      out.text = resp.body();
      out.subject = subject;

      return out;
    });
  }
}
