package models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.annotation.JsonIgnore
import database.generated.tables.records.CoursesRecord
import org.joda.time.DateTime

data class School(val code: String, val name: String, val subjects: List<Subject>)

data class Subject(val code: String, val courses: List<Course>)

/**
Data class for Course object with:
- Course name (String)
- Course number (int)
- Course subject code (type subject)
- Course sections (type List)
 */
data class Course(
    val name: String,
    val deptCourseId: String,
    private val subject: SubjectCode,
    val sections: List<Section>
) {
    @JsonIgnore
    fun getSubject(): String {
        return this.subject.subject
    }

    @JsonIgnore
    fun getSchool(): String {
        return this.subject.school
    }
}

/**
 * Section Object
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

/**
 * Meeting time for classes/sections
 */
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
