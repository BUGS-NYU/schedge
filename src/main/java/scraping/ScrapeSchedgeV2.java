package scraping;

import static utils.JsonMapper.*;
import static utils.Nyu.*;
import static utils.Try.*;

import api.v1.*;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.function.*;
import org.slf4j.*;
import utils.*;

public final class ScrapeSchedgeV2 {
  private static final Logger logger = LoggerFactory.getLogger("scraping.ScrapeSchedge2");

  private static final String LIST_SCHOOLS = "https://nyu.a1liu.com/api/schools/";
  private static final String COURSES = "https://nyu.a1liu.com/api/courses/";

  static final class ScrapeResult {
    String text;
    String subject;
  }

  public static ScrapeEvent.Result scrapeFromSchedge(Term term, Consumer<ScrapeEvent> consumer) {
    return scrapeFromSchedge(term, null, consumer);
  }

  public static ScrapeEvent.Result scrapeFromSchedge(
      Term term, List<String> inputSubjectList, Consumer<ScrapeEvent> consumer) {
    var client = HttpClient.newHttpClient();
    var termString = term.json();

    var schoolsUri = URI.create(LIST_SCHOOLS + termString);
    var request = HttpRequest.newBuilder().uri(schoolsUri).GET().build();
    var handler = HttpResponse.BodyHandlers.ofString();
    var resp = tcPass(() -> client.send(request, handler));
    var data = resp.body();

    var info = fromJson(data, SchoolInfoEndpoint.Info.class);
    var schools = info.schools;

    if (inputSubjectList == null) {
      inputSubjectList = new ArrayList<>();

      for (var school : info.schools) {
        for (var subject : school.subjects) {
          inputSubjectList.add(subject.code());
        }
      }
    }

    var iterable =
        Flowable.fromIterable(inputSubjectList)
            .parallel(5)
            .runOn(Schedulers.io())
            .map(subject -> getData(client, term, subject))
            .sequential()
            .filter(Objects::nonNull)
            .map(result -> Arrays.asList(fromJson(result, Course[].class)))
            .blockingIterable();

    return new ScrapeEvent.Result(term, schools, iterable);
  }

  private static String getData(HttpClient client, Term term, String subject) {
    try {
      long start = System.nanoTime();

      Try ctx = Try.Ctx();
      ctx.put("term", term);
      ctx.put("subject", subject);

      var uri = URI.create(COURSES + term.json() + "/" + subject);
      var request = HttpRequest.newBuilder().uri(uri).build();
      var handler = HttpResponse.BodyHandlers.ofString();

      var resp = ctx.log(() -> client.send(request, handler));

      long end = System.nanoTime();
      double duration = (end - start) / 1000000000.0;
      logger.info("Fetching subject={} took {} seconds", duration, subject);

      // This slows down the scrape, which is bad, but gives the production server some
      // breathing room, so that development doesn't accidentally DDOS the server.
      // TODO: Optimize the code enough that this isn't necessary anymore
      tcPass(() -> Thread.sleep(250));

      var body = resp.body();

      return body;
    } catch (Exception e) {
      logger.error("Error (subject={}): {}", subject, e.getMessage());
      return null;
    }
  }
}
