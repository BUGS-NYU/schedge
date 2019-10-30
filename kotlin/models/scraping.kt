package models

import org.joda.time.DateTime
import org.joda.time.Duration

data class CatalogEntry(
    val courseName: String,
    val subject: String,
    val courseId: Int,
    val deptCourseNumber: Int,
    val sections: List<CatalogSectionEntry>
)

data class CatalogSectionEntry(
    val registrationNumber: Int,
    val sectionNumber: Int,
    val type: SectionType,
    val instructor: String,
    val associatedWith: CatalogSectionEntry?,
    val status: SectionStatus,
    val meetings: List<Meeting>
) {
    override fun toString(): String {
        return "CatalogSectionEntry(registrationNumber=${registrationNumber}, sectionNumber=${sectionNumber}, type=${type}, instructor=${instructor}, associatedWith=${associatedWith?.registrationNumber}, status=${status}, meetings=${meetings})"
    }
}

data class Meeting(
    // TODO Convert this to org.joda.DateTime
    val beginDate: DateTime, // Begin date; contains date and time of first event.
    val duration: Duration, // Duration of meeting
    val activeDuration: Duration // How long after the begin that this event can start. Meetings implicitly meet weekly.
)

data class SectionResult(
  val description: String
)
