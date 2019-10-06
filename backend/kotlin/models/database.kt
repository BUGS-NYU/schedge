package models

import java.time.Duration

enum class SectionType {
    Lec, // Lecture
    Rec, // Recitation
    Lab, // Lab
    Sem, // Seminar
}

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
    val course: Course
)

data class Meeting(
    val date: DateTime,
    val duration: Duration,
    val repeat: Duration
)

