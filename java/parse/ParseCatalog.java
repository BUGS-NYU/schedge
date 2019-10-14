package parse;

import models.CatalogEntry;
import org.jsoup.nodes.Document;

import java.util.List;

public class ParseCatalog {
    /**
     * Get formatted course data from a catalog query result.
     */
    public static List<CatalogEntry> parse(Document data) {
    /*
    val xml = Jsoup.parse(courseData) ?: throw IOException("Jsoup.parse returned null")

        // Get all siblings of the primary head
        val elementList = xml.select("div.primary-head ~ *")
            ?: throw IOException("xml.select returned null")

        val output: MutableList<Pair<CourseAbbrev, MutableList<SectionAbbrev>>> = mutableListOf(
            Pair(
                ParseCourseListing.parse(elementList.first() ?: throw IOException("Course data is empty!")),
                mutableListOf()
            )
        )
        var current = output.last()

        elementList.asSequence().drop(1).forEach { element ->
            when {
                element.tagName() == "div" -> {
                    output += Pair(ParseCourseListing.parse(element), mutableListOf())
                    current = output.last()
                }
                else -> current.component2()
                    .add(ParseSectionListing.parse(element))
            }
        }

        return output
     */
        throw new UnsupportedOperationException("");
    }
}
