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
sealed class Section(val type: SectionType) {

    companion object {
        @JvmStatic
        fun getSection(
            registrationNumber: Int,
            sectionCode: String,
            instructor: String,
            type: SectionType,
            status: SectionStatus,
            meetings: List<Meeting>,
            recitations: List<Section>?
        ): Section {
            /*
            Instantiate different data objects based on the input
            @param: type
            @return Data object based on Section Type
             */
            return when (type) {
                SectionType.LEC -> Lecture(registrationNumber, sectionCode, instructor, status, meetings, recitations)
                SectionType.RCT -> {
                    require(recitations == null) { "Provided argument recitations=$recitations when type was not SectionType.LEC." }
                    Recitation(registrationNumber, sectionCode, instructor, status, meetings)
                }
                SectionType.LAB -> {
                    require(recitations == null) { "Provided argument recitations=$recitations when type was not SectionType.LEC." }
                    Lab(registrationNumber, sectionCode, instructor, status, meetings)
                }

                SectionType.STU -> {
                    require(recitations == null) { "Provided argument recitations=$recitations when type was not SectionType.LEC." }
                    Studio(registrationNumber, sectionCode, instructor, status, meetings)
                }
                SectionType.SIM -> {
                    require(recitations == null) { "Provided argument recitations=$recitations when type was not SectionType.LEC." }
                    Simulation(registrationNumber, sectionCode, instructor, status, meetings)
                }
                SectionType.CLI -> {
                    require(recitations == null) { "Provided argument recitations=$recitations when type was not SectionType.LEC." }
                    Clinic(registrationNumber, sectionCode, instructor, status, meetings)
                }
                SectionType.FLD -> {
                    require(recitations == null) { "Provided argument recitations=$recitations when type was not SectionType.LEC." }
                    Field(registrationNumber, sectionCode, instructor, status, meetings)
                }
                SectionType.WKS -> {
                    require(recitations == null) { "Provided argument recitations=$recitations when type was not SectionType.LEC." }
                    Workshop(registrationNumber, sectionCode, instructor, status, meetings)
                }
                SectionType.STI -> {
                    require(recitations == null) { "Provided argument recitations=$recitations when type was not SectionType.LEC." }
                    IndependentInstruction(registrationNumber, sectionCode, instructor, status, meetings)
                }
                SectionType.STG -> {
                    require(recitations == null) { "Provided argument recitations=$recitations when type was not SectionType.LEC." }
                    GroupInstruction(registrationNumber, sectionCode, instructor, status, meetings)
                }

                else -> {
                    require(recitations == null) { "Provided argument recitations=$recitations when type was not SectionType.LEC." }
                    Other(registrationNumber, sectionCode, instructor, status, type, meetings)
                }
            }
        }
    }

    /**
     * Different data class for different course component
     */
    data class Lecture(
        val registrationNumber: Int,
        val sectionCode: String,
        val instructor: String,
        val status: SectionStatus,
        val meetings: List<Meeting>,
        val recitations: List<Section>?
    ) : Section(SectionType.LEC)

    data class Recitation(
        val registrationNumber: Int,
        val sectionCode: String,
        val instructor: String,
        val status: SectionStatus,
        val meetings: List<Meeting>
    ) : Section(SectionType.RCT)

    data class Lab(
        val registrationNumber: Int,
        val sectionCode: String,
        val instructor: String,
        val status: SectionStatus,
        val meetings: List<Meeting>
    ) : Section(SectionType.LAB)

    data class Simulation(
        val registrationNumber: Int,
        val sectionCode: String,
        val instructor: String,
        val status: SectionStatus,
        val meetings: List<Meeting>
    ) : Section(SectionType.SIM)

    data class Clinic(
        val registrationNumber: Int,
        val sectionCode: String,
        val instructor: String,
        val status: SectionStatus,
        val meetings: List<Meeting>
    ) : Section(SectionType.CLI)

    data class Field(
        val registrationNumber: Int,
        val sectionCode: String,
        val instructor: String,
        val status: SectionStatus,
        val meetings: List<Meeting>
    ) : Section(SectionType.FLD)

    data class Workshop(
        val registrationNumber: Int,
        val sectionCode: String,
        val instructor: String,
        val status: SectionStatus,
        val meetings: List<Meeting>
    ) : Section(SectionType.WKS)

    data class IndependentInstruction(
        val registrationNumber: Int,
        val sectionCode: String,
        val instructor: String,
        val status: SectionStatus,
        val meetings: List<Meeting>
    ) : Section(SectionType.STI)

    data class Studio(
        val registrationNumber: Int,
        val sectionCode: String,
        val instructor: String,
        val status: SectionStatus,
        val meetings: List<Meeting>
    ) : Section(SectionType.STU)

    data class GroupInstruction(
        val registrationNumber: Int,
        val sectionCode: String,
        val instructor: String,
        val status: SectionStatus,
        val meetings: List<Meeting>
    ) : Section(SectionType.STG)

    data class Internship(
        val registrationNumber: Int,
        val sectionCode: String,
        val instructor: String,
        val status: SectionStatus,
        val meetings: List<Meeting>
    ) : Section(SectionType.INT)

    data class Colloquium(
        val registrationNumber: Int,
        val sectionCode: String,
        val instructor: String,
        val status: SectionStatus,
        val meetings: List<Meeting>
    ) : Section(SectionType.CLQ)

    class Other(
        val registrationNumber: Int,
        val sectionCode: String,
        val instructor: String,
        val status: SectionStatus,
        type: SectionType,
        val meetings: List<Meeting>
    ) : Section(type)
}

// A time that people meet for a class
data class Meeting(
    val beginDate: DateTime, // Begin date; contains date and time of first event.
    val duration: Duration, // Duration of meeting
    val endDate: DateTime // When the meeting stops repeating
) {

    constructor(beginDate: DateTime, duration: Long, endDate: DateTime) : this(
        beginDate, Duration(duration), endDate
    )

    @JsonValue
    fun toJson(): MeetingJson {
        return MeetingJson(beginDate.toString(), duration.toString(), endDate.toString())
    }
}

data class MeetingJson(val beginDate: String, val duration: String, val endDate: String)
