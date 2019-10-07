package models

import java.time.Duration

enum class SectionType {
    LEC, // Lecture
    REC, // Recitation
    LAB, // Lab
    SEM, // Seminar
}

private fun String.asResourceLines(): List<String> {
    val resource = object {}::class.java.getResource(this)
    println(resource)
    return resource.readText().lineSequence().filter { it.length > 0 }.toList()
}

val Subjects : List<String> = "/subjects.txt".asResourceLines()
val Schools : List<School> = "/schools.txt".asResourceLines().map {
    val (name, longName) = it.split(',')
    School(name, longName)
}

data class School(
    val abbrev: String,
    val name: String
)

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

