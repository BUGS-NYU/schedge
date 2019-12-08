package models

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import database.generated.tables.records.CoursesRecord
import org.joda.time.DateTime

data class School(val code: String, val name: String, val subjects: List<Subject>)

data class Subject(val code: String, val courses: List<Course>)

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

class Section(
  val registrationNumber: Int,
  val sectionCode: String,
  val instructor: String,
  val type: SectionType,
  val status: SectionStatus,
  val meetings: List<Meeting>,
  private val recitations: List<Section>?
  ) {

    init {
      require(type == SectionType.LEC || recitations == null) {
        "If the section type isn't a lecture, it can't have recitations!"
      }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    fun getRecitations(): List<Section>? {
        return recitations
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
        return MeetingJson(beginDate.toString("MM/dd/yyyy h:mma"), minutesDuration, endDate.toString("MM/dd/yyyy"))
    }
}

data class MeetingJson(val beginDate: String, val duration: Long, val endDate: String)
