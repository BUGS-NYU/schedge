package schedge

import org.jsoup.Jsoup
import schedge.models.Course
import schedge.models.Section
import schedge.parse.ParseCourse
import schedge.parse.ParseSection
import java.io.IOException

fun parseCourseData(courseData: String): List<Pair<Course, List<Section>>>? {
    val xml = Jsoup.parse(courseData) ?: throw IOException("Jsoup.parse returned null")

    val elementList = xml.select(
            "div.primary-head ~ *") // Get all siblings of the primary head

    val output : MutableList<Pair<Course, MutableList<Section>>> = mutableListOf()

    for (i in elementList.indices) {
        val element = elementList[i]

        if (element.tagName() != "div") {
            output.add(Pair<Course, MutableList<Section>>(ParseCourse.parse(element), mutableListOf()))
        } else {
            // First element wasn't a `div` for some reason.
            assert(output.isEmpty())
            output[output.size - 1]
                    .component2()
                    .add(ParseSection.parse(element))
        }
    }

    return output
}

