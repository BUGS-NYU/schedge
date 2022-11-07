package scraping;

import static scraping.PSCoursesParser.*;
import static utils.ArrayJS.*;
import static utils.Nyu.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import me.tongfei.progressbar.ProgressBar;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.slf4j.*;
import utils.*;

public final class PeopleSoftClassSearch {
  static Logger logger =
      LoggerFactory.getLogger("scraping.PeopleSoftClassSearch");

  public static final class SubjectElem {
    public final String schoolName;
    public final String code;
    public final String name;
    public final String action;

    SubjectElem(String school, String code, String name, String action) {
      this.schoolName = school;
      this.code = code;
      this.name = name;
      this.action = action;
    }

    @Override
    public String toString() {
      return "SubjectElem(schoolName=" + schoolName + ",code=" + code +
          ",name=" + name + ",action=" + action + ")";
    }
  }

  public static final class CoursesForTerm {
    public final ArrayList<School> schools = new ArrayList<>();
    public final ArrayList<Course> courses = new ArrayList<>();
  }

  public static final class FormEntry {
    public final String key;
    public final String value;

    public FormEntry(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }

  final Try ctx = Try.Ctx(logger);

  public PeopleSoftClassSearch() {}

  public ArrayList<School> scrapeSchools(Term term) {
    ctx.put("term", term);

    return ctx.log(() -> {
      var ps = new PSClient();
      var resp = ps.navigateToTerm(term).get();
      var subjects = parseTermPage(resp.body());
      return translateSubjects(subjects);
    });
  }

  public ArrayList<Course> scrapeSubject(Term term, String subjectCode) {
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

        return parseSubject(ctx, responseBody, subject.code);
      }
    });
  }

  /**
   * @param term The term to scrape
   * @param bar Nullable progress bar to output progress to
   */
  public CoursesForTerm scrapeTerm(Term term, ProgressBar bar) {
    return ctx.log(() -> scrapeTermInternal(term, bar));
  }

  CoursesForTerm scrapeTermInternal(Term term, ProgressBar bar)
      throws ExecutionException, IOException, InterruptedException {
    var out = new CoursesForTerm();
    if (bar != null) {
      bar.setExtraMessage("fetching subject list...");
      bar.maxHint(-1);
    }

    ctx.put("term", term);

    var ps = new PSClient();

    ArrayList<SubjectElem> subjects;
    {
      var resp = ps.navigateToTerm(term).get();
      subjects = parseTermPage(resp.body());
    }

    out.schools.addAll(translateSubjects(subjects));
    ctx.put("schools", out.schools);

    if (bar != null) {
      bar.maxHint(subjects.size() + 1);
    }

    for (var subject : subjects) {
      if (bar != null) {
        bar.setExtraMessage("fetching " + subject.code);
        bar.step();
      }

      ctx.put("subject", subject);

      while (true)
        try {
          var resp = ps.fetchSubject(subject).get();
          var responseBody = resp.body();

          var courses = parseSubject(ctx, responseBody, subject.code);
          out.courses.addAll(courses);
          break;
        } catch (ExecutionException e) {
          Thread.sleep(10_000);
          ps = new PSClient();
          ps.navigateToTerm(term).get();
        }
    }

    return out;
  }

  ArrayList<SubjectElem> parseTermPage(String responseBody) {
    var doc = Jsoup.parse(responseBody, PSClient.MAIN_URL);

    var field = doc.expectFirst("#win0divNYU_CLASS_SEARCH");
    var cdata = (CDataNode)field.textNodes().get(0);

    doc = Jsoup.parse(cdata.text(), PSClient.MAIN_URL);
    var results = doc.expectFirst("#win0divRESULTS");
    var group = results.expectFirst("div[id=win0divGROUP$0]");

    var out = new ArrayList<SubjectElem>();
    for (var child : group.children()) {
      var school = child.expectFirst("h2").text();

      var schoolTags = child.select("div.ps_box-link");
      for (var schoolTag : schoolTags) {
        var schoolTitle = schoolTag.text();
        var parts = schoolTitle.split("\\(");

        var titlePart = parts[0].trim();
        var codePart = parts[1];
        codePart = codePart.substring(0, codePart.length() - 1);

        var action = schoolTag.id().substring(7);

        out.add(new SubjectElem(school, codePart, titlePart, action));
      }
    }

    return out;
  }
}
