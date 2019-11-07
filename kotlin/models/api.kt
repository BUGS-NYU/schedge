package models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import org.joda.time.DateTime

data class Course(
    val nyuCourseId: Long,
    val name: String,
    val deptCourseNumber: Int,
    private val subject: SubjectCode,
    val sections: List<Section>
) {

    @JsonCreator
    private constructor(
        @JsonProperty("nyu_course_id") nyuCourseId: Long,
        @JsonProperty("name") name: String,
        @JsonProperty("dept_course_number") deptCourseNumber: Int,
        @JsonProperty("subject") subject: String,
        @JsonProperty("school") school: String,
        @JsonProperty("sections") sections: List<Section>
    ) : this(nyuCourseId, name, deptCourseNumber, SubjectCode.getUnchecked(subject, school), sections)

    fun getSubject(): String {
      return this.subject.subject
    }

    fun getSchool(): String {
      return this.subject.school
    }

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
