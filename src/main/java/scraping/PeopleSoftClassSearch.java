package scraping;

import static scraping.PSCoursesParser.*;
import static utils.ArrayJS.*;
import static utils.Nyu.*;

import java.io.IOException;
import java.net.*;
import java.net.http.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.concurrent.*;
import me.tongfei.progressbar.ProgressBar;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.slf4j.*;
import utils.Try;

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

  static String MAIN_URL =
      "https://sis.nyu.edu/psc/csprod/EMPLOYEE/SA/c/NYU_SR.NYU_CLS_SRCH.GBL";

  static URI MAIN_URI = URI.create(MAIN_URL);

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
        ps.incrementStateNum();
        ps.formMap.put("ICAction", subject.action);

        resp = ps.client.send(post(MAIN_URI, ps.formMap),
                              HttpResponse.BodyHandlers.ofString());
        var responseBody = resp.body();

        return parseSubject(ctx, responseBody, subjectCode);
      }
    });
  }

  // @TODO: There's a more good way to do this, where we just imitate a user
  // navigating the page normally; but I am tired and don't want to do it
  // right now.
  //
  //                          - Albert Liu, Nov 03, 2022 Thu 16:08
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

    var resp = ps.navigateToTerm(term).get();

    var subjects = parseTermPage(resp.body());

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
      var fut = ps.fetchSubject(subject);
      resp = fut.get();
      //
      //      Thread.sleep(5_000);
      //
      //      ps.incrementStateNum();
      //      ps.formMap.put("ICAction", subject.action);
      //
      //      resp = ps.client.send(post(MAIN_URI, ps.formMap), handler);
      var responseBody = resp.body();

      var courses = parseSubject(ctx, responseBody, subject.code);
      out.courses.addAll(courses);

      //      ps.incrementStateNum();
      //      ps.formMap.put("ICAction", "NYU_CLS_DERIVED_BACK");
      //
      //      resp = ps.client.send(post(MAIN_URI, ps.formMap), handler);
      //      responseBody = resp.body();
    }

    return out;
  }

  ArrayList<SubjectElem> parseTermPage(String responseBody) {
    var doc = Jsoup.parse(responseBody, MAIN_URL);

    var field = doc.expectFirst("#win0divNYU_CLASS_SEARCH");
    var cdata = (CDataNode)field.textNodes().get(0);

    doc = Jsoup.parse(cdata.text(), MAIN_URL);
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

  static HttpRequest post(URI uri, HashMap<String, String> body) {
    String s = PSClient.formEncode(body);

    return HttpRequest.newBuilder()
        .uri(uri)
        .timeout(Duration.of(30, ChronoUnit.SECONDS))
        .setHeader("User-Agent", PSClient.USER_AGENT)
        .setHeader("Content-Type", "application/x-www-form-urlencoded")
        .POST(HttpRequest.BodyPublishers.ofString(s))
        .build();
  }
}
