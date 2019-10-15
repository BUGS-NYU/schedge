package models

import java.time.Duration

// import org.jetbrains.exposed.dao.*

data class CourseAbbrev( // Gotten from catalog
    val name: String,
    val subject: String,
    val courseId: Int,
    val deptCourseNumber: Int // 7 in BIOL-UA 7
)

data class SectionAbbrev( // Gotten from catalog
    val registrationNumber: Int,
    val type: SectionType
)

data class Course( // Gotten from course page
    val name: String
)

data class Section( // Gotten from course page
    val course: Course,
    val type: SectionType,
    val status: SectionStatus,
    val meetings: List<Meeting>,
    val associatedWith: Section?
)

data class Meeting(
    val instructor: String,
    val begin: DateTime, // Begin date; contains date and time of first event.
    val duration: Duration, // Duration of meeting
    val activeDuration: Duration, // How long after the begin that this event can start. Meetings implicitly meet weekly.
    val days: Days // The days that this meeting happens on
) {
}

