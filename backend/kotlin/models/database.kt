package models

import java.time.Duration
import org.jetbrains.exposed.dao.*

data class CourseAbbrev( // Gotten from catalog
    val name: String
)

data class SectionAbbrev( // Gotten from catalog
    val course: CourseAbbrev
)

data class Course( // Gotten from course page
    val name: String
)

data class Section( // Gotten from course page
    val course: Course,
    val type: SectionType,
    val meetings: List<Meeting>,
    val associatedWith: Section?
)

data class Meeting(
    val instructor: String,
    val date: DateTime,
    val duration: Duration,
    val repeat: Duration
)

