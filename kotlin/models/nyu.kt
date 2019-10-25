package models

import asResourceLines

enum class SectionType {
    LEC, // Lecture
    RCT, // Recitation
    LAB, // Lab
    SEM, // Seminar
    IND;  // independent study

    fun getName(): String {
        return when (this) {
            LEC -> "Lecture"
            RCT -> "Recitation"
            LAB -> "Lab"
            SEM -> "Seminar"
            IND -> "Independent Study"
        }
    }
}

enum class SectionStatus {
  Open, // Open
  Closed, // Closed
  Cancelled; // Cancelled

    fun isOpen(): Boolean {
        return when (this) {
            Open -> true
            Closed -> false
            Cancelled -> false
        }
    }
}

private val AvailableSchools : Map<String, String> = "/schools.txt".asResourceLines().map {
    val (abbrev, name) = it.split(',')
    Pair(abbrev, name)
}.toMap()

private val AvailableSubjects : Map<String, Set<String>> = "subjects.txt".asResourceLines().map {
    val (subj, school) = it.split('-')
    Pair(subj, school)
}.let {
    val availSubjects = mutableMapOf<String, MutableSet<String>>()
    it.forEach { (subj, school) ->
        availSubjects.getOrPut(school) {
            mutableSetOf()
        }.add(subj)
    }
    availSubjects
}

class School(
    abbrevString: String
) {

    val abbrev = abbrevString.toUpperCase()

    init {
        require(AvailableSchools.containsKey(abbrev)) {
            "School must be valid!"
        }
    }

    override fun toString(): String {
      return abbrev
    }
}

class Subject(
    abbrevString: String,
    schoolString: String
) {

    val abbrev = abbrevString.toUpperCase()
    val school = schoolString.toUpperCase()

    init {
        require(AvailableSubjects.containsKey(school)) {
            "School must be valid"
        }
        require(AvailableSubjects[school]!!.contains(abbrev)) {
            "Subject must be valid!"
        }
    }

    override fun toString(): String {
      return abbrev
    }
}

/**
 * One of the semesters in a year.
 */
enum class Semester {
    January,
    Spring,
    Summer,
    Fall;

    companion object {
        // Turns enum into integer that conforms to NYU's conventions.
        fun fromInt(id: Int): Semester {
            return when (id) {
                2 -> January
                4 -> Spring
                6 -> Summer
                8 -> Fall
                else -> throw IllegalArgumentException("ID can only be one of {2, 4, 6, 8} (got $id)")
            }
        }
    }

    // Turns enum into integer that conforms to NYU's conventions.
    fun toInt(): Int {
        return when (this) {
            January -> 2
            Spring -> 4
            Summer -> 6
            Fall -> 8
        }
    }
}

/**
 * A term, like Summer 19. year is a positive number representing the year.
 * For example, 2019 represents the year 2019.
 */
class Term(private val sem: Semester, private val year: Int) {
    companion object {
        fun fromId(id: Int): Term {
            require(id >= 0) { "Can't create Term with negative ID" }
            return Term(Semester.fromInt(id % 10), (id / 10) + 1900)
        }
    }

    init {
        require(year >= 0) { "Can't create a Term with negative year" }
    }

    val id
        get(): Int {
            return (year - 1900) * 10 + sem.toInt()
        }


    override fun toString(): String {
        return "Term(${sem}, ${year})"
    }
}
