package register;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ParseEnroll {
  // @ToDO: Change this to the appropriate return value
  public static void parseRegistrationNumber(String data) {
    Document secData = Jsoup.parse(data);
    Element body = secData.selectFirst("body");
    Element section = body.selectFirst("section.main > section");
    Elements sections = section.select("div");
    for (Element element : sections) {
      if (element.text().equals("Results") || element.text().equals("Okay")) {
        continue;
      }
      System.out.println(element.text());
    }
  }
}
