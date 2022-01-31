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

  private static final String SCHEDGE_URL = "https://schedge.a1liu.com/";

  public static Stream<Course> scrapeFromSchedge(Term term) {
    BiFunction<SubjectCode, Integer, Future<String>> func = (subject, idx) -> {
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
      return Client.send(request, (resp, throwable) -> {
        if (resp == null) {
          logger.error("Error (subject={}): {}", subject,
                       throwable.getMessage());

          return null;
        }

        String text = resp.getResponseBody();
        return text;
      });
    };

    return StreamSupport
        .stream(new SimpleBatchedFutureEngine<SubjectCode, String>(
                    SubjectCode.allSubjects().listIterator(), 20, func)
                    .spliterator(),
                false)
        .flatMap(text -> {
          if (text == null) {
            return new ArrayList<Course>().stream();
          }

          List<Course> courses = JsonMapper.fromJsonArray(text, Course.class);
          return courses.stream();
        });
  }
}
