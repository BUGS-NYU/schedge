package database

import models.SectionType
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.dao.LongIdTable

// All migrations that have been applied to the table
// insert only
object Migrations : IntIdTable() {
    val dateAdded = datetime("date_added").index()
    val description = text("description")
}

// All tables in the Schema
val Tables = arrayOf(Courses, Sections, Meetings)

// Courses that you can take at NYU
// Contains information like "MATH-UA 120" and "Discrete Math"
object Courses : LongIdTable() {
    val courseId = integer("course_id").index()
    val name = varchar("name", length = 100)
    val subject = varchar("subject", length = 8)
    val deptCourseNumber = integer("dept_course_number").index()
    val termId = integer("term_id")
}

// Sections that you can register for at NYU
object Sections : LongIdTable() {
    val registrationNumber = integer("registration_number").index()
    val courseId = reference("course_id", Courses).index()
    val sectionNumber = integer("section_number")
    val termId = integer("term_id")
    val type = enumeration("type", klass = SectionType::class)
    val associatedWith = reference("associated_with", Sections.id).nullable().index()
}

// A class meeting
object Meetings : LongIdTable() {
    val sectionId = reference("section_id", Sections).index()
    val locationId = varchar("location", length = 5)
    val instructor = varchar("instructor", length = 50)
    val date = datetime("start")
    val duration = integer("duration") // Duration of event in minutes
}

