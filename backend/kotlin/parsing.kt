import org.jsoup.Jsoup
import models.CourseAbbrev
import models.SectionAbbrev
import models.Section
import models.Course
import parse.ParseCourseListing
import parse.ParseSectionListing
import parse.ParseCourse
import parse.ParseSection
import java.io.IOException

class Parser(val logger: Logging = Logging.getLogger(Logging.WARN)) {


    fun parseListing(courseData: String): List<Pair<CourseAbbrev, List<SectionAbbrev>>> {
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
    }

    fun parseSection(sectionData: String): Pair<Course, Section> {
        val xml = Jsoup.parse(sectionData).let {
            it ?: logger.error("Got a null value from Jsoup's parser!", ::IOException)
        }.let {
            it.select("section.main").first()
                ?: logger.error("Couldn't find Course or Section data.", ::IOException)
        }

        return Pair(
            ParseCourse.parse(xml),
            ParseSection.parse(xml.let {
                it.select("section").first()
                    ?: logger.error("Couldn't find Section data.", ::IOException)
            })
        )
    }
}
