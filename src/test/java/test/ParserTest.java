package test;

import static actions.CopyTermFromProduction.*;
import static utils.JsonMapper.*;
import static utils.Nyu.*;
import static utils.Try.*;

import api.*;
import io.javalin.testtools.JavalinTest;
import org.junit.*;
import org.slf4j.*;
import scraping.PSCoursesParser;
import utils.Utils;

public class ParserTest {
  static Logger logger = LoggerFactory.getLogger("test.ParserTest");
  @Test
  public void testParserBasic() {
    // sp2021
    // CSCI-UA

    String html = Utils.readResource("/csci-ua-sp2021.html");
    var ctx = Ctx(logger);
    PSCoursesParser.parseSubject(ctx, html, "CSCI-UA", e -> {
      switch (e.kind) {
      case WARNING:
        Assert.fail();
      }
    });
  }

  @Test
  public void testSimpleScrape() {
    copyTermFromProduction(SchedgeVersion.V2, Term.fromString("ja2022"));
  }
}