package services;

import java.io.IOException;
import java.util.*;
import models.SubjectCode;
import nyu.SectionStatus;
import nyu.Term;
import org.jetbrains.annotations.NotNull;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraping.models.Section;
import scraping.models.SectionAttribute;

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

  public static SectionAttribute parse(@NotNull String rawData) {
    logger.info("parsing raw catalog section data into SectionAttribute...");

    rawData = rawData.trim();

    if (rawData.equals("")) {
      logger.warn("Got bad data: empty string");
      return null; // the course doesn't exist
    }

    Document doc = Jsoup.parse(rawData);
    Element failed = doc.selectFirst("div.alert.alert-info");
    if (failed != null) {
      logger.warn("Got bad data: " + failed.text());
      return null; // the course doesn't exist
    }

    doc.select("a").unwrap();
    doc.select("i").unwrap();
    doc.select("b").unwrap();
    Element outerDataSection = doc.selectFirst("body > section.main");
    Element header = outerDataSection.selectFirst("> header.page-header");
    Element innerDataSection = outerDataSection.selectFirst("> section");
    Element courseNameDiv = innerDataSection.selectFirst("> div.primary-head");
    String courseName = courseNameDiv.text();
    Elements dataDivs =
        innerDataSection.select("> div.section-content.clearfix");
    Map<String, String> secData = parseSectionAttributes(dataDivs);

    return parsingElements(secData, courseName);
  }

  static @NotNull Map<String, String>
  parseSectionAttributes(@NotNull Elements attributeData) {
    Map<String, String> map = new HashMap<>();
    for (Element e : attributeData) {
      if (e.child(0).text().equals("Topic") &&
          e.child(1).text().contains("Room")) {
        continue;
      }
      map.put(e.child(0).text(), e.child(1).text());
    }
    return map;
  }

  public static @NotNull SectionAttribute
  parsingElements(Map<String, String> secData, String courseName) {
    String units = secData.get("Units");
    double minUnits = 0, maxUnits;
    if (units.contains("-")) {
      minUnits = Double.parseDouble(units.split(" - ")[0]);
      maxUnits = Double.parseDouble(units.split(" - ")[1].split(" ")[0]);
    } else {
      maxUnits = Double.parseDouble(units.split(" ")[0]);
    }

    courseName +=
        secData.containsKey("Topic") ? " " + secData.get("Topic") : "";

    return new SectionAttribute(
        courseName.equals("") ? null : courseName,
        Integer.parseInt(secData.get("Class Number")),
        SectionStatus.parseStatus(secData.get("Status")),
        secData.get("Location"), secData.get("Description"),
        secData.get("Instruction Mode"), secData.get("Instructor(s)"), minUnits,
        maxUnits, secData.get("Grading"),
        secData.getOrDefault("Notes", "See Description. None otherwise"),
        secData.get("Room"));
  }

  public static Map<String, String> update(String rawData) {
    logger.info("parsing raw catalog section data...");
    Document doc = Jsoup.parse(rawData);
    doc.select("a").unwrap();
    doc.select("i").unwrap();
    doc.select("b").unwrap();
    Element outerDataSection = doc.selectFirst("body > section.main");
    Element innerDataSection = outerDataSection.selectFirst("> section");
    //      String courseName =
    //              innerDataSection.selectFirst("> div.primary-head").text();
    Elements dataDivs =
        innerDataSection.select("> div.section-content.clearfix");
    Map<String, String> secData = parseSectionAttributes(dataDivs);
    String units = secData.get("Units");
    String minUnits = "", maxUnits = "";
    if (units.contains("-")) {
      minUnits = units.split(" - ")[0];
      maxUnits = units.split(" - ")[1].split(" ")[0];
    } else {
      maxUnits = units.split(" ")[0];
    }
    String courseName =
        secData.containsKey("Topic") ? "" + secData.get("Topic") : "";
    secData.put("sectionName", courseName);
    secData.put("minUnits", minUnits);
    secData.put("maxUnits", maxUnits);
    return secData;
  }
}
