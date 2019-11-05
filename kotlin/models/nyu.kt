package models

import asResourceLines

enum class SectionType {
    LEC, // Lecture
    RCT, // Recitation
    LAB, // Lab
    SEM, // Seminar
    RSC, // Unknown
    FLD, // Unknown
    SIM, // Unknown
    CLI, // Unknown
    STU, // Unknown
    STI, // Unknown
    CLQ, // Unknown
    WKS, // Unknown
    IND;  // independent study

    fun getName(): String {
        return when (this) {
            LEC -> "Lecture"
            RCT -> "Recitation"
            LAB -> "Lab"
            SEM -> "Seminar"
            IND -> "Independent Study"
            else -> "Unknown"
        }
    }
}

enum class SectionStatus {
    Open, // Open
    Closed, // Closed
    WaitList, // Waitlist
    Cancelled; // Cancelled

    companion object {
        @JvmStatic
        fun parseStatus(status: String): SectionStatus {
            return when (status) {
                "Wait List" -> WaitList
                else -> valueOf(status)
            }
        }
    }

    fun isOpen(): Boolean {
        return this == Open
    }

}

private val AvailableSubjects: Map<String, Set<String>> = "subjects.txt".asResourceLines().map {
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

class SubjectCode {

    companion object {
        fun allSubjects(forSchool: String? = null): Sequence<SubjectCode> {
            return if (forSchool == null) {
                AvailableSubjects.asSequence().map { pair ->
                    pair.value.asSequence().map {
                        SubjectCode(it, pair.key, Unit)
                    }
                }.flatten()
            } else {
                val subjects = AvailableSubjects[forSchool.toUpperCase()]
                require(subjects != null) { "Must provide a valid school code!" }
                subjects.asSequence().map { SubjectCode(it, forSchool, Unit) }
            }
        }
    }

    val subject: String
    val school: String
    val abbrev: String
        inline get() {
            return "$subject-$school"
        }

    // No constructor without checks
    private constructor(abbrevString: String, schoolString: String, unused: Unit) {
        subject = abbrevString.toUpperCase()
        school = schoolString.toUpperCase()
        unused.hashCode() // To silence errors
    }

    constructor(abbrevString: String, schoolString: String) {
        subject = abbrevString.toUpperCase()
        school = schoolString.toUpperCase()
        val subjects = AvailableSubjects[school]
        require(subjects != null) { "School must be valid" }
        require(subjects.contains(subject)) { "Subject must be valid!" }
    }

    constructor(abbrevString: String) {
        val (subject, school) = abbrevString.toUpperCase().split('-')
        this.subject = subject
        this.school = school
        val subjects = AvailableSubjects[this.school]
        require(subjects != null) { "School must be valid" }
        require(subjects.contains(this.subject)) { "Subject must be valid!" }
    }

    override fun toString(): String = abbrev
    override fun hashCode(): Int {
        return abbrev.hashCode()
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
        @JvmStatic
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
