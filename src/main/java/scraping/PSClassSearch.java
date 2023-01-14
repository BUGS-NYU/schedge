package scraping;

import static scraping.PSCoursesParser.*;
import static utils.ArrayJS.*;
import static utils.Nyu.*;

import io.reactivex.rxjava3.core.Flowable;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.slf4j.*;
import utils.*;

public final class PSClassSearch {
  static Logger logger = LoggerFactory.getLogger("scraping.PeopleSoftClassSearch");

  public static final class SubjectElem {
    public final String schoolName;
    public final String schoolCode;
    public final String code;
    public final String name;
    public final String action;

    SubjectElem(String school, String schoolCode, String code, String name, String action) {
      this.schoolName = school;
      this.schoolCode = schoolCode;
      this.code = code;
      this.name = name;
      this.action = action;
    }

    @Override
    public String toString() {
      return "SubjectElem(schoolName="
          + schoolName
          + ",schoolCode="
          + schoolCode
          + ",code="
          + code
          + ",name="
          + name
          + ",action="
          + action
          + ")";
    }
  }

  public static final class FormEntry {
    public final String key;
    public final String value;

    public FormEntry(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }

  public static ArrayList<School> scrapeSchools(Term term) {
    var ctx = Try.Ctx(logger);

    ctx.put("term", term);

    return ctx.log(
        () -> {
          var ps = new PSClient();
          var resp = ps.navigateToTerm(term);
          var subjects = parseTermPage(resp.body());
          return translateSubjects(subjects);
        });
  }

  public static ArrayList<Course> scrapeSubject(Term term, String subjectCode) {
    var ctx = Try.Ctx(logger);

    ctx.put("term", term);
    ctx.put("subject", subjectCode);

    return ctx.log(
        () -> {
          var ps = new PSClient();
          var resp = ps.navigateToTerm(term);
          var subjects = parseTermPage(resp.body());

          var subject = find(subjects, s -> s.code.equals(subjectCode));
          if (subject == null) throw new RuntimeException("Subject not found: " + subjectCode);

          {
            resp = ps.fetchSubject(subject).get();
            var responseBody = resp.body();

            return parseSubject(
                ctx,
                responseBody,
                subject.code,
                e -> {
                  if (e instanceof ScrapeEvent.Warn w) {
                    logger.warn(w.message);
                  }
                });
          }
        });
  }

  /**
   * @param term The term to scrape
   * @param consumer Nullable progress bar to output progress to
   */
  public static TermScrapeResult scrapeTerm(Term term, Consumer<ScrapeEvent> consumer) {
    var ctx = Try.Ctx(logger);

    ctx.put("term", term);
    var ps = Utils.ref(new PSClient());

    consumer.accept(ScrapeEvent.message(null, "Fetching subject list..."));
    consumer.accept(ScrapeEvent.hintChange(-1));

    var subjects =
        ctx.log(
            () -> {
              var resp = ps.value.navigateToTerm(term);
              return parseTermPage(resp.body());
            });

    var schools = ctx.log(() -> translateSubjects(subjects));

    consumer.accept(ScrapeEvent.hintChange(subjects.size() + 1));
    consumer.accept(ScrapeEvent.progress(1));

    var results =
        Flowable.fromIterable(subjects)
            .map(
                subject -> {
                  System.out.println("FUFUFUFUFU");
                  ctx.put("subject", subject);
                  consumer.accept(ScrapeEvent.subject(subject.code));

                  for (int i = 0; i < 10; i++) {
                    try {
                      var resp = ps.value.fetchSubject(subject).get();
                      var body = resp.body();

                      var parsed = parseSubject(ctx, body, subject.code, consumer);

                      consumer.accept(ScrapeEvent.progress(1));

                      return parsed;
                    } catch (CancellationException e) {
                      // When this method is cancelled through task cancellation, don't
                      // keep retrying
                      //
                      // God I fucking hate exceptions
                      //
                      //                      - Albert Liu, Nov 27, 2022 Sun 00:32
                      throw e;
                    } catch (Exception e) {
                      // Catch other types of exceptions because scraping is nowhere near
                      // stable and likely never will be

                      consumer.accept(
                          ScrapeEvent.warning(subject.code, "ERROR: " + e.getMessage()));
                      consumer.accept(ScrapeEvent.warning(subject.code, Utils.stackTrace(e)));

                      Thread.sleep(10_000);

                      ps.value = new PSClient();
                      ps.value.navigateToTerm(term);
                    }
                  }

                  var resp = ps.value.fetchSubject(subject).get();
                  var body = resp.body();

                  List<Course> parsed = parseSubject(ctx, body, subject.code, consumer);

                  consumer.accept(ScrapeEvent.progress(1));

                  return parsed;
                });

    return new TermScrapeResult.Impl(term, schools, results.blockingIterable());
  }

  public static ArrayList<SubjectElem> parseTermPage(String responseBody) {
    var doc = Jsoup.parse(responseBody, PSClient.MAIN_URL);

    var field = doc.expectFirst("#win0divNYU_CLASS_SEARCH");
    var cdata = (CDataNode) field.textNodes().get(0);

    doc = Jsoup.parse(cdata.text(), PSClient.MAIN_URL);
    var results = doc.expectFirst("#win0divRESULTS");
    var group = results.expectFirst("div[id=win0divGROUP$0]");

    var out = new ArrayList<SubjectElem>();
    for (var child : group.children()) {
      var schoolH2 = child.expectFirst("h2");
      var school = schoolH2.text();

      var schoolTags = child.select("div.ps_box-link");
      for (var schoolTag : schoolTags) {
        var schoolTitle = schoolTag.text();
        var parts = schoolTitle.split("\\(");

        var titlePart = parts[0].trim();
        var codePart = parts[1];
        codePart = codePart.substring(0, codePart.length() - 1);

        var subjectParts = codePart.split("_")[0].split("-");
        var schoolCode = subjectParts[subjectParts.length - 1];

        var action = schoolTag.id().substring(7);

        out.add(new SubjectElem(school, schoolCode, codePart, titlePart, action));
      }
    }

    return out;
  }
}
