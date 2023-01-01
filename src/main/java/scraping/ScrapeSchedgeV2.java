package scraping;

import static utils.JsonMapper.*;
import static utils.Nyu.*;
import static utils.Try.*;

import api.v1.*;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import org.slf4j.*;
import utils.*;

public final class ScrapeSchedgeV2 extends TermScrapeResult {
  private static Logger logger =
      LoggerFactory.getLogger("scraping.ScrapeSchedge2");

  private static final String LIST_SCHOOLS =
      "https://nyu.a1liu.com/api/schools/";
  private static final String COURSES = "https://nyu.a1liu.com/api/courses/";

  static final class ScrapeResult {
    String text;
    String subject;
  }

  public static TermScrapeResult
  scrapeFromSchedge(Term term, Consumer<ScrapeEvent> consumer) {
    return scrapeFromSchedge(term, null, consumer);
  }

  public static TermScrapeResult
  scrapeFromSchedge(Term term, List<String> inputSubjectList,
                    Consumer<ScrapeEvent> consumer) {
    return new ScrapeSchedgeV2(term, inputSubjectList, consumer);
  }

  private final ArrayList<School> schools;
  private final HttpClient client = HttpClient.newHttpClient();
  private final Iterator<String> subjects;
  private final FutureEngine<ScrapeResult> engine = new FutureEngine<>();

  private ScrapeSchedgeV2(Term term, List<String> inputSubjectList,
                          Consumer<ScrapeEvent> consumer) {
    super(term, consumer, Try.Ctx(logger));

    var termString = term.json();

    var schoolsUri = URI.create(LIST_SCHOOLS + termString);
    var request = HttpRequest.newBuilder().uri(schoolsUri).GET().build();
    var handler = HttpResponse.BodyHandlers.ofString();
    var resp = tcPass(() -> client.send(request, handler));
    var data = resp.body();

    var info = fromJson(data, SchoolInfoEndpoint.Info.class);
    this.schools = info.schools;

    if (inputSubjectList == null) {
      inputSubjectList = new ArrayList<>();

      for (var school : info.schools) {
        for (var subject : school.subjects) {
          inputSubjectList.add(subject.code());
        }
      }
    }

    subjects = inputSubjectList.iterator();

    for (int i = 0; i < 20; i++) {
      if (subjects.hasNext()) {
        engine.add(getData(client, term, subjects.next()));
      }
    }
  }

  @Override
  public ArrayList<School> getSchools() {
    return this.schools;
  }

  @Override
  public boolean hasNext() {
    return this.engine.hasNext();
  }

  @Override
  public ArrayList<Course> next() {
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

      return out;
    }

    return null;
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
