package schedge.models

enum class Semester {
  January,
  Spring,
  Fall,
  Summer;

  companion object {
    fun from_int(id: Int): Semester {
      return when (id) {
        2 -> Semester.January
        4 -> Semester.Spring
        6 -> Semester.Summer
        8 -> Semester.Fall
        else -> throw IllegalArgumentException("Used invalid id")
      }
    }
  }

  fun as_int(): Int {
    return when (this) {
      Semester.January-> 2
      Semester.Spring -> 4
      Semester.Summer -> 6
      Semester.Fall -> 8
    }
  }
}


/**
 * A term, like Summer 19. year is a positive number representing the year.
 * For example, 2019 represents the year 2019.
 */
class Term(val sem: Semester, val year: Int) {

  companion object {
    fun from_id(id: Int): Term {
      val year = id / 10
      return Term(Semester.from_int(id - year), year)
    }
  }

  init {
    if (year < 0)
      throw IllegalArgumentException("Can't create a Term with negative year")
  }

  val id
    get(): Int {
      return (year - 1900) * 10 + sem.as_int()
    }

  override fun toString(): String {
    return "Term(${sem}, ${year})"
  }
}
