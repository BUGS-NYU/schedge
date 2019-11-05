package models

import com.fasterxml.jackson.annotation.JsonValue
import org.joda.time.DateTime

data class Course(
    val nyuCourseId: Long,
    val name: String,
    val deptCourseNumber: Long,
    val sections: List<Section>
) {
}

sealed class Section(val type: SectionType) {

    companion object {

        @JvmStatic
        fun getSection(
            registrationNumber: Int,
            sectionNumber: Int,
            instructor: String,
            type: SectionType,
            status: SectionStatus,
            meetings: List<Meeting>,
            recitations: List<Section>?
        ): Section {
            return when (type) {
                SectionType.LEC -> Lecture(registrationNumber, sectionNumber, instructor, status, meetings, recitations)
                SectionType.RCT -> {
                    require(recitations == null) { "Provided argument recitations=$recitations when type was not SectionType.LEC." }
                    Recitation(registrationNumber, sectionNumber, instructor, status, meetings)
                }
                SectionType.LAB -> {
                    require(recitations == null) { "Provided argument recitations=$recitations when type was not SectionType.LEC." }
                    Lab(registrationNumber, sectionNumber, instructor, status, meetings)
                }
                else -> {
                    require(recitations == null) { "Provided argument recitations=$recitations when type was not SectionType.LEC." }
                    Other(registrationNumber, sectionNumber, instructor, status, type, meetings)
                }
            }
        }
    }

    data class Lecture(
        val registrationNumber: Int,
        val sectionNumber: Int,
        val instructor: String,
        val status: SectionStatus,
        val meetings: List<Meeting>,
        val recitations: List<Section>?
    ) : Section(SectionType.LEC)

    data class Recitation(
        val registrationNumber: Int,
        val sectionNumber: Int,
        val instructor: String,
        val status: SectionStatus,
        val meetings: List<Meeting>
    ) : Section(SectionType.RCT)

    data class Lab(
        val registrationNumber: Int,
        val sectionNumber: Int,
        val instructor: String,
        val status: SectionStatus,
        val meetings: List<Meeting>
    ) : Section(SectionType.LAB)

    class Other(
        val registrationNumber: Int,
        val sectionNumber: Int,
        val instructor: String,
        val status: SectionStatus,
        type: SectionType,
        val meetings: List<Meeting>
    ) : Section(type)
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