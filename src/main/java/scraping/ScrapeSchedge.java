package scraping;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.uri.Uri;
import org.slf4j.*;
import types.*;
import utils.*;

public final class ScrapeSchedge {
  private static Logger logger =
      LoggerFactory.getLogger("scraping.ScrapeSchedge");

  // @TODO this will eventually scrape directly from the new API instead of
  // the old one
  //                        - Albert Liu, Jan 25, 2022 Tue 18:32 EST
  private static final String SCHEDGE_URL = "https://schedge.a1liu.com/";

  public static List<List<Course>> scrapeFromSchedge(Term term) {
    BiFunction<Subject, Integer, Future<String>> func = (subject, idx) -> {
      String school = subject.schoolCode;
      String major = subject.code.split("-")[0];

      // @TODO Fix this hack to work around weird behavior from V1 and NYU
      if (school.contentEquals("UI")) {
        school = "SHU";
      }

      String[] components =
          new String[] {"" + term.year, term.semString(), school, major};

      Uri uri =
          Uri.create(SCHEDGE_URL + String.join("/", components) + "?full=true");

      Request request = new RequestBuilder().setUri(uri).build();

      long start = System.nanoTime();

      return Client.send(request, (resp, throwable) -> {
        long end = System.nanoTime();
        double duration = (end - start) / 1000000000.0;
        logger.info("Fetching took {} seconds: subject={}", duration, subject);

        if (resp == null) {
          logger.error("Error (subject={}): {}", subject,
                       throwable.getMessage());

          return null;
        }

        String text = resp.getResponseBody();
        return text;
      });
    };

    Iterator<Subject> subjects = Subject.allSubjects().listIterator();
    SimpleBatchedFutureEngine<Subject, String> engine =
        new SimpleBatchedFutureEngine<>(subjects, 20, func);

    ArrayList<List<Course>> output = new ArrayList<>();
    for (String text : engine) {
      if (text == null) {
        continue;
      }

      List<Course> courses = JsonMapper.fromJsonArray(text, Course.class);
      output.add(courses);
    }

    return output;
  }
}
