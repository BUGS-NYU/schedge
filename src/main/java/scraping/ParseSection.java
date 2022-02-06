package scraping.parse;

import java.util.*;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.*;
import types.Section;
import types.SectionStatus;
import utils.Utils;

public class ParseSection {
  private static Logger logger =
      LoggerFactory.getLogger("scraping.parse.ParseSection");
  private static Pattern pattern = Pattern.compile("[0-9]");

  private static Map<String, String> buildings;

  static {
    buildings = new HashMap<>();
    for (String line : Utils.asResourceLines("/building.txt")) {
      String[] entry = line.split(",", 2);

      buildings.put(entry[0], entry[1]);
    }
  }
}
