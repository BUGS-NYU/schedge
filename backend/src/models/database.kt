package schedge.models

import kotlin.experimental.or
import kotlin.experimental.and
import schedge.models.TimeOfDay
import schedge.models.Days

/**
 * Course object, used both in the database and in memory.
 *
 * Fields:
 * - abbrev: Abbreviated name
 * - name: Course name
 */
data class Course(val abbrev: String, val name: String)

data class Section(val days: Days, val time: TimeOfDay, val days2: Days?, val time2: TimeOfDay?)


