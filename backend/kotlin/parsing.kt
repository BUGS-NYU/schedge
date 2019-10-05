import org.jsoup.Jsoup
import models.Course
import models.Section
import models.CourseLong
import models.SectionLong
import parse.ParseCourse
import parse.ParseSection
import java.io.IOException

fun parseCourseData(courseData: String): List<Pair<Course, List<Section>>> {
    val xml = Jsoup.parse(courseData) ?: throw IOException("Jsoup.parse returned null")

    // Get all siblings of the primary head
    val elementList = xml.select("div.primary-head ~ *")
        ?: throw IOException("xml.select returned null")

    val output: MutableList<Pair<Course, MutableList<Section>>> = mutableListOf(
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

fun parseSectionData(sectionData: String): Pair<CourseLong, SectionLong> {
    val xml = Jsoup.parse(sectionData).let {
        it ?: throw IOException("Jsoup.parse returned null")
    }.let {
        it.select("section.main").first()
            ?: throw IOException("Couldn't find data")
    }
    throw UnsupportedOperationException("")
}
