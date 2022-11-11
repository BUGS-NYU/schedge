package scraping;

import static utils.Nyu.*;

import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;
import org.slf4j.*;
import utils.*;

public final class ScrapeSchedge2 {
  private static Logger logger =
      LoggerFactory.getLogger("scraping.ScrapeSchedge2");

  private static final String LIST_TERMS = "https://nyu.a1liu.com/api/terms";
  private static final String LIST_SCHOOLS =
      "https://nyu.a1liu.com/api/schools/";
  private static final String COURSES = "https://nyu.a1liu.com/api/courses/";

  public static List<Course> scrapeFromSchedge(Term term) {
    var subjects = Subject.allSubjects().listIterator();
    var client = HttpClient.newHttpClient();

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

      List<Course> courses = JsonMapper.fromJsonArray(text, Course.class);
      output.addAll(courses);
    }

    return output;
  }

  private static Future<String> getData(HttpClient client, Term term,
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

      String text = resp.body();
      return text;
    });
  }
}
