package parse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import models.CatalogEntry;
import models.CatalogSectionEntry;
import models.SectionType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ParseCatalog {
  /**
   * Get formatted course data from a catalog query result.
   */
  public static List<CatalogEntry> parse(Document data) throws IOException {
    Elements elementList = data.select("div.primary-head ~ *");
    if (elementList == null)
      throw new IOException("xml.select returned null");
    else if (elementList.size() == 0)
      throw new IOException("Course data is empty!");
    if (!elementList.get(0).tagName().equals("div"))
      throw new IOException("NYU sent back data we weren't expecting.");

    ArrayList<CatalogEntry> output = new ArrayList<>();

    Course current = parseCourseNode(elementList.get(0));
    ArrayList<CatalogSectionEntry> sections = new ArrayList<>();

    for (int i = 1; i < elementList.size(); i++) {
      Element e = elementList.get(i);
      if (e.tagName().equals("div")) {
        output.add(current.getCatalogEntry(sections));
        current = parseCourseNode(e);
        sections = new ArrayList<>();
      } else {
        sections.add(parseSectionNode(e));
      }
    }
    return output;
  }

  public static Course parseCourseNode(Element divTag) throws IOException {
    String text = divTag.text(); // MATH-UA 9 - Algebra and Calculus
    int textIndex1 = text.indexOf(' '), textIndex2 = text.indexOf(" - ");
    if (textIndex1 < 0)
      throw new IOException("Couldn't find character ' ' in divTag.text");
    if (textIndex2 < 0)
      throw new IOException("Couldn't find substring \" - \" in divTag.text");

    String subject = text.substring(0, textIndex1);
    Integer deptCourseNumber =
        Integer.parseInt(text.substring(textIndex1, textIndex2));
    String courseName = text.substring(textIndex2 + 3);

    // <div class="secondary-head class-title-header" id=MATHUA9129903>
    int idIndex;
    String idString = divTag.attr("id");
    for (idIndex = 0; idIndex < idString.length(); idIndex++)
      if (idString.charAt(idIndex) <= '9' && idString.charAt(idIndex) >= '0')
        break;
    Integer courseId =
        Integer.parseInt(idString.substring(idIndex, idString.length()));

    return new Course(courseName, subject, courseId, deptCourseNumber);
  }
  public static CatalogSectionEntry parseSectionNode(Element anchorTag) {
    return null;
  }

  private static class Course {
    private String courseName;
    private String subject;
    private Integer courseId;
    private Integer deptCourseNumber;

    Course(String courseName, String subject, Integer courseId,
           Integer deptCourseNumber) {
      this.courseName = courseName;
      this.subject = subject;
      this.courseId = courseId;
      this.deptCourseNumber = deptCourseNumber;
    }

    public CatalogEntry
    getCatalogEntry(ArrayList<CatalogSectionEntry> sections) {
      return new CatalogEntry(this.courseName, this.subject, this.courseId,
                              this.deptCourseNumber, sections);
    }
  }
}
