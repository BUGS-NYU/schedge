package schedge.models

import schedge.models.TimeOfDay
import schedge.models.Days
import org.jsoup.nodes.Element
// import org.jsoup.nodes.Elements

/**
 * Course object, used both in the database and in memory.
 *
 * Fields:
 * - abbrev: Abbreviated name
 * - name: Course name
 */
data class Course(
  val abbrev: String,
  val name: String
)

data class Section(
  val courseId: Int,
  val days: Days,
  val time: TimeOfDay,
  val days2: Days?,
  val time2: TimeOfDay?
)

