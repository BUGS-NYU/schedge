package schedge;

import static schedge.wrapper.ConvenienceKt.*;

import java.io.IOException;
import java.lang.UnsupportedOperationException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import schedge.models.Course;
import schedge.models.Section;
import schedge.parsers.CourseParser;
import schedge.parsers.SectionParser;
import schedge.wrapper.KtPair;

public class Parser {

  /**
   * Get formatted term data.
   */
  public static ArrayList<KtPair<Course, ArrayList<Section>>>
  parseTermData(String data) throws IOException {

    Document xml = Jsoup.parse(data);
    if (xml == null)
      throw new IOException("Jsoup.parse returned null");

    Elements elementList = xml.select(
        "div.primary-head ~ *"); // Get all siblings of the primary head

    ArrayList<KtPair<Course, ArrayList<Section>>> output =
        new ArrayList<KtPair<Course, ArrayList<Section>>>();

    for (int i = 0; i < elementList.size(); i++) {
      Element element = elementList.get(i);

      if (!element.tagName().equals("div")) {
        output.add(new KtPair<Course, ArrayList<Section>>(
            parseCourseData(element), new ArrayList<Section>()));
      } else {
        // First element wasn't a `div` for some reason.
        assert output.isEmpty();
        output.get(output.size() - 1)
            .getComponent2()
            .add(parseSectionData(element));
      }
    }

    return output;
  }

  /**
   * Get formatted course data.
   */
  public static Course parseCourseData(Element data) {
    return CourseParser.parseCourseData(data);
  }

  /**
   * Get formatted section data.
   */
  public static Section parseSectionData(Element data) {
    return SectionParser.parseSectionData(data);
  }
}
