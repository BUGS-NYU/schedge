package scraping.parse;

import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.*;

public class ParseSchoolSubjects {
  private static Logger logger =
      LoggerFactory.getLogger("scraping.parse.ParseSection");

  public static String parseSchoolSubject(String rawData) {
    logger.info("parsing raw school data...");
    Document doc = Jsoup.parse(rawData);
    Elements scripts = doc.select("script");
    String output = "";
    for (Element script : scripts) {
      String scriptHTML = script.html();
      if (scriptHTML.contains("classSearch")) {
        output = scriptHTML.substring(scriptHTML.indexOf('{') + 1,
                                      scriptHTML.lastIndexOf('}'));
        output =
            output.substring(output.indexOf('{'), output.lastIndexOf('}') + 1);
      }
    }
    return output;
  }

  public static Map<String, String> parseSchool(String rawData) {
    String HTML = parseSchoolSubject(rawData);
    List<String> schools = new ArrayList<>();
    List<String> descriptions = new ArrayList<>();
    Map<String, String> map = new HashMap<>();
    String output = HTML.substring(HTML.lastIndexOf("acad_groups"),
                                   HTML.indexOf("nyuSchoolsUrl"));
    output = output.substring(output.indexOf("[") + 1, output.lastIndexOf("]"));
    String[] outputs = output.split(",");
    for (String value : outputs) {
      if (value.contains("acad_group")) {
        schools.add(
            value.substring(value.indexOf(":") + 2, value.lastIndexOf("\"")));
      } else {
        descriptions.add(
            value.substring(value.indexOf(":") + 2, value.lastIndexOf("\"")));
      }
    }

    for (int i = 0; i < schools.size(); i++) {
      map.put(schools.get(i), descriptions.get(i));
    }

    return map;
  }

  public static HashMap<String, String> parseSubject(String rawData) {
    String HTML = parseSchoolSubject(rawData);
    String output = HTML.substring(HTML.indexOf("acad_groups"),
                                   HTML.lastIndexOf("acad_groups"));
    output = output.substring(output.indexOf(":") + 1, output.lastIndexOf("]"));
    String[] innerData = output.split("\",\"");
    List<String> subjects = new ArrayList<>();
    List<String> descriptions = new ArrayList<>();
    HashMap<String, String> map = new HashMap<>();
    for (String value : innerData) {
      if (value.contains("subject")) {
        subjects.add(value.substring(value.lastIndexOf(":") + 2));
      } else if (value.contains("descr")) {
        descriptions.add(value.substring(value.indexOf(":") + 2));
      }
    }

    for (int i = 0; i < subjects.size(); i++) {
      map.put(subjects.get(i), descriptions.get(i));
    }
    return map;
  }
}
