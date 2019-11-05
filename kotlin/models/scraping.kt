package models

import com.fasterxml.jackson.annotation.JsonValue
import org.joda.time.DateTime

data class CatalogEntry(
    val courseName: String,
    val subject: String,
    val courseId: Long,
    val deptCourseNumber: Long,
    val sections: List<CatalogSectionEntry>
) {
    fun toCourse(): Course {

        TODO()
    }
}

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
    val beginDate: DateTime, // Begin date; contains date and time of first event.
    val duration: Duration, // Duration of meeting
    val activeDuration: Duration // How long after the begin that this event can start. Meetings implicitly meet weekly.
) {
    companion object {}

    constructor(beginDate: DateTime, duration: Long, activeDuration: Long) : this(
        beginDate,
        Duration(duration),
        Duration(activeDuration)
    )

    @JsonValue
    fun toJson(): MeetingJson {
        return MeetingJson(beginDate.toString(), duration.toString(), activeDuration.toString())
    }
}

data class MeetingJson(val beginDate: String, val duration: String, val activeDuration: String)