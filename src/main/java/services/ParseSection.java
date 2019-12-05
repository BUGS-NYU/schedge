package services;

import java.time.DayOfWeek;
import java.io.IOException;
import java.util.Arrays;
import java.util.*;
import kotlin.text.StringsKt;
import models.*;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.UtilsKt;
import org.jetbrains.annotations.NotNull;

/**
 * Parses a catalog string in a stream.
 *
 * @author Albert Liu
 */
public class ParseSection {
  private static Logger logger =
      LoggerFactory.getLogger("services.ParseSection");
  private static DateTimeFormatter timeParser =
      DateTimeFormat.forPattern("MM/dd/yyyy h:mma");

  public static @NotNull String parse(@NotNull String rawData)
      throws IOException {
    logger.info("parsing raw catalog data...");
    Document doc = Jsoup.parse(rawData);
    Element outerDataSection = doc.selectFirst("body > section.main");
    Element header = outerDataSection.selectFirst("> header.page-header");
    Element innerDataSection = outerDataSection.selectFirst("> section");
    String courseName =
        innerDataSection.selectFirst("> div.primary-head").text();
    Elements dataDivs =
        innerDataSection.select("> div.section-content.clearfix");
    return courseName;
  }
}
