import org.jsoup.Jsoup
import models.CourseAbbrev
import models.SectionAbbrev
import models.Section
import models.Course
import mu.KLogger
import mu.KotlinLogging
import parse.ParseCourseListing
import parse.ParseSectionListing
import parse.ParseCourse
import parse.ParseSection
import java.io.IOException

class Parser(val logger: KLogger = KotlinLogging.logger {}) {

    fun parseCatalog(courseData: String): List<Pair<CourseAbbrev, List<SectionAbbrev>>> {
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
            if (it == null) {
                logger.error(IOException("Got a null value from Jsoup's parser!")) {
                    "Got a null value from Jsoup's parser!"
                }
            }
            it

        }.let {
            val first = it.selectFirst("section.main")
            if (first == null) {
                logger.error(IOException("Couldn't find Course or Section data.")) {
                    "Got a null value from Jsoup's parser!"
                }
            }
            first
        }

        return Pair(
            ParseCourse.parse(xml),
            ParseSection.parse(xml.let {
                val first = it.selectFirst("section")
                if (first == null) {
                    logger.error("Couldn't find Section data.")
                    throw IOException()
                }
                first
            })
        )
    }
}
