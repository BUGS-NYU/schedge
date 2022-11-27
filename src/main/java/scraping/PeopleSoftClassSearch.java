package scraping;

import static scraping.PSCoursesParser.*;
import static utils.ArrayJS.*;
import static utils.Nyu.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import me.tongfei.progressbar.ProgressBar;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.slf4j.*;
import utils.*;

public final class PeopleSoftClassSearch {
  static Logger logger =
      LoggerFactory.getLogger("scraping.PeopleSoftClassSearch");

  /* Real fucking stupid implementation of whatever you desire
   * to call this. Event listening, observer pattern, whatever.
   *
   *                  - Albert Liu, Nov 27, 2022 Sun 02:32
   */
  public static final class ScrapeEvent {
    public enum Kind {
      WARNING,
      MESSAGE,
      SUBJECT_START,
      PROGRESS,
      HINT_CHANGE;
    }

    public final Kind kind;
    public final String currentSubject;
    public final String message;
    public final int value;

    private ScrapeEvent(Kind kind, String currentSubject, String message,
                        int value) {
      this.kind = kind;
      this.currentSubject = currentSubject;
      this.message = message;
      this.value = value;
    }

    static ScrapeEvent warning(String subject, String message) {
      return new ScrapeEvent(Kind.WARNING, subject, message, 0);
    }

    static ScrapeEvent message(String subject, String message) {
      return new ScrapeEvent(Kind.MESSAGE, subject, message, 0);
    }

    static ScrapeEvent subject(String subject) {
      return new ScrapeEvent(Kind.SUBJECT_START, subject, "Fetching " + subject,
                             0);
    }

    static ScrapeEvent progress(int progress) {
      return new ScrapeEvent(Kind.PROGRESS, null, null, progress);
    }

    static ScrapeEvent hintChange(int hint) {
      return new ScrapeEvent(Kind.HINT_CHANGE, null, null, hint);
    }
  }

  public static final class SubjectElem {
    public final String schoolName;
    public final String schoolCode;
    public final String code;
    public final String name;
    public final String action;

    SubjectElem(String school, String schoolCode, String code, String name,
                String action) {
      this.schoolName = school;
      this.schoolCode = schoolCode;
      this.code = code;
      this.name = name;
      this.action = action;
    }

    @Override
    public String toString() {
      return "SubjectElem(schoolName=" + schoolName +
          ",schoolCode=" + schoolCode + ",code=" + code + ",name=" + name +
          ",action=" + action + ")";
    }
  }

  public static final class CoursesForTerm
      implements Iterator<ArrayList<Course>> {
    private final ArrayList<SubjectElem> subjects;
    private final Consumer<ScrapeEvent> consumer;
    private final Try ctx;
    private final Term term;

    private PSClient ps;
    private int index = 0;

    private CoursesForTerm(Term term, Consumer<ScrapeEvent> consumer, Try ctx) {
      if (consumer == null) {
        this.consumer = e -> {};
      } else {
        this.consumer = consumer;
      }

      this.term = term;
      this.ctx = ctx;

      this.ps = new PSClient();

      consumer.accept(ScrapeEvent.message(null, "Fetching subject list..."));
      consumer.accept(ScrapeEvent.hintChange(-1));

      var resp = ctx.log(() -> {
        ctx.put("term", term);

        return ps.navigateToTerm(term).get();
      });

      this.subjects = ctx.log(() -> parseTermPage(resp.body()));

      consumer.accept(ScrapeEvent.hintChange(subjects.size() + 1));
      consumer.accept(ScrapeEvent.progress(1));
    }

    @Override
    public boolean hasNext() {
      return index < subjects.size();
    }

    @Override
    public ArrayList<Course> next() {
      var subject = subjects.get(index);
      index += 1;

      ctx.put("subject", subject);

      consumer.accept(ScrapeEvent.subject(subject.code));

      var parsed = Try.tcPass(() -> {
        for (int i = 0; i < 10; i++) {
          try {
            var resp = ps.fetchSubject(subject).get();
            var body = resp.body();

            return parseSubject(ctx, body, subject.code, consumer);
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

            Thread.sleep(10_000);
            consumer.accept(
                ScrapeEvent.warning(subject.code, "ERROR: " + e.getMessage()));
            consumer.accept(
                ScrapeEvent.warning(subject.code, Utils.stackTrace(e)));
            ps = new PSClient();
            ps.navigateToTerm(term).get();
          }
        }

        var resp = ps.fetchSubject(subject).get();
        var body = resp.body();

        return parseSubject(ctx, body, subject.code, consumer);
      });

      consumer.accept(ScrapeEvent.progress(1));

      return parsed;
    }

    public ArrayList<School> getSchools() {
      return ctx.log(() -> translateSubjects(subjects));
    }

    // @Note: This happens to allow JSON serialization of this object to
    // correctly run scraping, by forcing the serialization of the object to
    // run this method, which then consumes the iterator. It's stupid.
    //
    //                                  - Albert Liu, Nov 10, 2022 Thu 22:21
    public ArrayList<Course> getCourses() {
      var courses = new ArrayList<Course>();
      while (this.hasNext()) {
        courses.addAll(this.next());
      }

      return courses;
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

    return ctx.log(() -> {
      var ps = new PSClient();
      var resp = ps.navigateToTerm(term).get();
      var subjects = parseTermPage(resp.body());
      return translateSubjects(subjects);
    });
  }

  public static ArrayList<Course> scrapeSubject(Term term, String subjectCode) {
    var ctx = Try.Ctx(logger);

    ctx.put("term", term);
    ctx.put("subject", subjectCode);

    return ctx.log(() -> {
      var ps = new PSClient();
      var resp = ps.navigateToTerm(term).get();
      var subjects = parseTermPage(resp.body());

      var subject = find(subjects, s -> s.code.equals(subjectCode));
      if (subject == null)
        throw new RuntimeException("Subject not found: " + subjectCode);

      {
        resp = ps.fetchSubject(subject).get();
        var responseBody = resp.body();

        return parseSubject(ctx, responseBody, subject.code, e -> {
          switch (e.kind) {
          case WARNING:
            logger.warn(e.message);
            break;
          default:
            break;
          }
        });
      }
    });
  }

  /**
   * @param term The term to scrape
   * @param bar Nullable progress bar to output progress to
   */
  public static CoursesForTerm scrapeTerm(Term term,
                                          Consumer<ScrapeEvent> bar) {
    var ctx = Try.Ctx(logger);
    return new CoursesForTerm(term, bar, ctx);
  }

  static ArrayList<SubjectElem> parseTermPage(String responseBody) {
    var doc = Jsoup.parse(responseBody, PSClient.MAIN_URL);

    var field = doc.expectFirst("#win0divNYU_CLASS_SEARCH");
    var cdata = (CDataNode)field.textNodes().get(0);

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

        var schoolCode = codePart.split("_")[0].split("-")[1];

        var action = schoolTag.id().substring(7);

        out.add(
            new SubjectElem(school, schoolCode, codePart, titlePart, action));
      }
    }

    return out;
  }
}
