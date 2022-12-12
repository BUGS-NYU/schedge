package test;

import static utils.Try.*;

import org.junit.*;
import org.slf4j.*;
import scraping.PSClassSearch;
import scraping.PSCoursesParser;
import utils.ArrayJS;
import utils.Utils;

public class ParserTest {
  static Logger logger = LoggerFactory.getLogger("test.ParserTest");
  @Test
  public void testParserBasic() {
    var html = Utils.readResource("/csci-ua-sp2021.html.snap");
    var ctx = Ctx(logger);
    PSCoursesParser.parseSubject(ctx, html, "CSCI-UA", e -> {
      switch (e.kind) {
      case WARNING:
        Assert.fail();
      }
    });
  }

  @Test
  public void testParseExternalSchools() {
    var html = Utils.readResource("/term-sp2022.html.snap");
    var subjects = PSClassSearch.parseTermPage(html);

    var bocc = ArrayJS.find(subjects, s -> s.code.equals("BOCC"));
    Assert.assertEquals(bocc.schoolName, "Academic College");
    Assert.assertEquals(bocc.schoolCode, "BOCC");
    Assert.assertEquals(bocc.name, "Bocconi University");

    var yons = ArrayJS.find(subjects, s -> s.code.equals("YONS"));
    Assert.assertEquals(yons.schoolName, "Academic College");
    Assert.assertEquals(yons.schoolCode, "YONS");
    Assert.assertEquals(yons.name, "Yonsei University Courses");
  }

  // https://github.com/A1Liu/schedge/issues/216
  @Test
  public void testIssue216() {
    var html = Utils.readResource("/sts-uy-ja2023.html.snap");
    var ctx = Ctx(logger);
    var output = PSCoursesParser.parseSubject(ctx, html, "STS-UY", e -> {
      switch (e.kind) {
      case WARNING:
        Assert.fail();
      }
    });

    var course = ArrayJS.find(output, c -> c.deptCourseId.equals("2144"));
    Assert.assertEquals(course.name, "Ethics and Technology");
  }
}