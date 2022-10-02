package scraping;

import static types.Nyu.*;

import java.util.*;
import java.util.concurrent.*;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.uri.Uri;
import org.slf4j.*;
import utils.*;

public final class ScrapeSchedge {
  private static Logger logger =
      LoggerFactory.getLogger("scraping.ScrapeSchedge");

  // @TODO this will eventually scrape directly from the new API instead of
  // the old one
  //                        - Albert Liu, Jan 25, 2022 Tue 18:32 EST
  private static final String SCHEDGE_URL = "https://schedge.a1liu.com/";

  public static List<Course> scrapeFromSchedge(AsyncHttpClient client,
                                               Term term) {
    var subjects = types.Subject.allSubjects().listIterator();

    var engine = new FutureEngine<String>();
    for (int i = 0; i < 20; i++) {
      if (subjects.hasNext()) {
        engine.add(getData(client, term, subjects.next().code));
      }
    }

    var output = new ArrayList<Course>();
    for (String text : engine) {
      if (subjects.hasNext()) {
        engine.add(getData(client, term, subjects.next().code));
      }

      if (text == null) {
        continue;
      }

      List<Course> courses = JsonMapper.fromJsonArray(text, Course.class);
      output.addAll(courses);
    }

    return output;
  }

  private static Future<String> getData(AsyncHttpClient client, Term term,
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

    Uri uri =
        Uri.create(SCHEDGE_URL + String.join("/", components) + "?full=true");

    Request request = new RequestBuilder().setUri(uri).build();

    long start = System.nanoTime();

    var fut = client.executeRequest(request).toCompletableFuture();
    return fut.handleAsync((resp, throwable) -> {
      long end = System.nanoTime();
      double duration = (end - start) / 1000000000.0;
      logger.info("Fetching took {} seconds: subject={}", duration, subject);

      if (resp == null) {
        logger.error("Error (subject={}): {}", subject, throwable.getMessage());

        return null;
      }

      String text = resp.getResponseBody();
      return text;
    });
  }
}
