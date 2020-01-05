package services;

import java.time.DayOfWeek;
import java.io.IOException;
import java.util.Arrays;
import java.util.*;

import api.models.Meeting;
import kotlin.text.StringsKt;
import models.*;
import mu.KLogger;
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
import scraping.models.SectionAttribute;
import utils.UtilsKt;
import org.jetbrains.annotations.NotNull;

/**
 * Parses a section string.
 *
 * @author Albert Liu
 */
public class ParseSection {
  private static Logger logger =
      LoggerFactory.getLogger("services.ParseSection");
  private static DateTimeFormatter timeParser =
      DateTimeFormat.forPattern("MM/dd/yyyy h:mma");

  public static @NotNull
  SectionAttribute parse(@NotNull String rawData)
      throws IOException {
    logger.info("parsing raw catalog data...");
    Document doc = Jsoup.parse(rawData);
    doc.select("a").unwrap();
    doc.select("i").unwrap();
    Element outerDataSection = doc.selectFirst("body > section.main");
    Element header = outerDataSection.selectFirst("> header.page-header");
    Element innerDataSection = outerDataSection.selectFirst("> section");
    String courseName =
        innerDataSection.selectFirst("> div.primary-head").text();
    Elements dataDivs =
        innerDataSection.select("> div.section-content.clearfix");
    Map<String, String> secData = parseSectionAttributes(dataDivs);
    //Haven't checked for special case but seems to work for general ones. Will keep for now
    return parsingElements(secData, courseName);
  }

  static @NotNull Map<String, String>
  parseSectionAttributes(@NotNull Elements attributeData) {
    Map<String, String> map = new HashMap<>();
    for (Element e : attributeData) {
      if(e.child(0).text().equals("Topic") && e.child(1).text().contains("Room")) {
        continue;
      }
      map.put(e.child(0).text(), e.child(1).text());
    }
    return map;
  }

  public static @NotNull SectionAttribute parsingElements(Map<String, String> secData, String courseName) {
    String units = secData.get("Units");
    int minUnits, maxUnits = 0;
    if(units.contains("-")) {
      minUnits = Integer.parseInt(units.split(" - ")[0]);
      maxUnits = Integer.parseInt(units.split(" - ")[1].split(" ")[0]);
    } else {
      minUnits = 0;
      maxUnits = Integer.parseInt(units.split(" ")[0]);
    }
    courseName += secData.containsKey("Topic") ? " " + secData.get("Topic") : "";
    return new SectionAttribute(courseName, Integer.parseInt(secData.get("Class Number")),
            SectionStatus.parseStatus(secData.get("Status")), secData.get("Campus"), secData.get("Description"),
            secData.get("Instruction Mode"), secData.get("Instructor(s)"),
            minUnits, maxUnits, secData.get("Grading"),
            secData.containsKey("Notes") ? secData.get("Notes") : "See Description. None otherwise", secData.get("Room"));
  }
}
