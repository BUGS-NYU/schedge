package models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import database.generated.tables.records.CoursesRecord
import org.joda.time.DateTime

/**
Data class for Course object with:
- Course name (String)
- Course number (int)
- Course subject code (type subject)
- Course sections (type List)
 */
data class Course(
    val name: String,
    val deptCourseNumber: Int,
    private val subject: SubjectCode,
    val sections: List<Section>
) {

    @JsonCreator
    private constructor(
        @JsonProperty("name") name: String,
        @JsonProperty("dept_course_number") deptCourseNumber: Int,
        @JsonProperty("subject") subject: String,
        @JsonProperty("school") school: String,
        @JsonProperty("sections") sections: List<Section>
    ) : this(name, deptCourseNumber, SubjectCode.getUnchecked(subject, school), sections)

    fun getSubject(): String {
        return this.subject.subject
    }

    fun getSchool(): String {
        return this.subject.school
    }
}


/**
 * Sealed class Section that contains:
 * - Registration Number
 * - Section Code
 * - Instructor
 * - Section Type
 * - Section Status
 * - Meetings
 * - Recitation
 * - including other data classes based on classes
 * @param SectionType enum object
 */
class Section(
  val registrationNumber: Int,
  val sectionCode: String,
  val instructor: String,
  val type: SectionType,
  val status: SectionStatus,
  val meetings: List<Meeting>,
  val recitations: List<Section>?
  ) {

    init {
      require(type == SectionType.LEC || recitations == null) {
        "If the section type isn't a lecture, it can't have recitations!"
      }
    }
}

// A time that people meet for a class
data class Meeting(
    val beginDate: DateTime, // Begin date; contains date and time of first event.
    val minutesDuration: Long, // Duration of meeting
    val endDate: DateTime // When the meeting stops repeating
) {

    @JsonValue
    fun toJson(): MeetingJson {
        return MeetingJson(beginDate.toString(), minutesDuration, endDate.toString())
    }
}

data class MeetingJson(val beginDate: String, val duration: Long, val endDate: String)
