package models

/**
 * One of the semesters in a year.
 */
enum class Semester {
    January,
    Spring,
    Fall,
    Summer;

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
