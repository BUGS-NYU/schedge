package schedge.models

/**
 * Course object, used both in the database and in memory.
 *
 * Fields:
 * - abbrev: Abbreviated name
 * - name: Course name
 */
data class Course(
        val courseId: Long,
        val abbrev: String,
        val name: String
)

data class Section(
        val registrationNumber: Long,
        val courseId: Long,
        val days: Days,
        val time: TimeOfDay,
        val days2: Days?,
        val time2: TimeOfDay?
)

