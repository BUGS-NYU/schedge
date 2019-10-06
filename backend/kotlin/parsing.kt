import org.jsoup.Jsoup
import models.CourseAbbrev
import models.SectionAbbrev
import models.Section
import models.Course
import parse.ParseCourse
import parse.ParseSection
import java.io.IOException

fun parseListing(courseData: String): List<Pair<CourseAbbrev, List<SectionAbbrev>>> {
    val xml = Jsoup.parse(courseData) ?: throw IOException("Jsoup.parse returned null")

    // Get all siblings of the primary head
    val elementList = xml.select("div.primary-head ~ *")
        ?: throw IOException("xml.select returned null")

    val output: MutableList<Pair<CourseAbbrev, MutableList<SectionAbbrev>>> = mutableListOf(
        Pair(
            ParseCourse.parse(elementList.first() ?: throw IOException("Course data is empty!")),
            mutableListOf()
        )
    )
    var current = output.last()

    elementList.asSequence().drop(1).forEach { element ->
        when {
            element.tagName() == "div" -> {
                output += Pair(ParseCourse.parse(element), mutableListOf())
                current = output.last()
            }
            else -> current.component2()
                .add(ParseSection.parse(element))
        }
    }

    return output
}

fun parseSection(sectionData: String): Pair<Course, Section> {
    val xml = Jsoup.parse(sectionData).let {
        it ?: throw IOException("Jsoup.parse returned null")
    }.let {
        it.select("section.main").first()
            ?: throw IOException("Couldn't find data")
    }
    throw UnsupportedOperationException("")
}
