package database

import models.SectionStatus
import models.SectionType
import org.jetbrains.exposed.dao.IntIdTable

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
object Courses : IntIdTable() {
    val courseId = integer("course_id").index()
    val name = varchar("name", length = 100)
    val subject = varchar("subject", length = 8)
    val deptCourseNumber = integer("dept_course_number").index()
}

// Sections that you can register for at NYU
object Sections : IntIdTable() {
    val registrationNumber = integer("registration_number").index()
    val courseId = reference("course_id", Courses.id)
    val sectionNumber = integer("section_number")
    val type = enumeration("type", klass = SectionType::class)
    val associatedWith = reference("associated_with", Sections.id).nullable().index()
}

// A class meeting
object Meetings : IntIdTable() {
    val sectionId = reference("section_id", Sections.id).index()
    val locationId = varchar("location", length = 5)
    val instructor = varchar("instructor", length = 50)
    val date = datetime("start")
    val duration = integer("duration") // Duration of event in minutes
}

