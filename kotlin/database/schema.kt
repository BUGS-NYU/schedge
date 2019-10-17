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
val Tables = arrayOf(Courses, Sections, Locations, Meetings)

// Courses that you can take at NYU
// Contains information like "MATH-UA 120" and "Discrete Math"
object Courses : IntIdTable() {
    val nyuCourseId = integer("nyu_course_id").index() // Column<Long>
    val courseId = integer("course_id").index() // Column<Long>
    val abbrev = varchar("abbreviation", length = 10) // Column<String>
    val name = varchar("name", length = 100) // Column<String>
    val description = text("description")
}

// Sections that you can register for at NYU
object Sections : IntIdTable() {
    val registrationNumber = integer("registration_number").index()
    val courseId = reference("course_id", Courses.id)
    val type = enumerationByName("type", length = 3, klass = SectionType::class)
    val status = enumerationByName("status", length = 3, klass = SectionStatus::class)
    val associatedWith = reference("associated_with", Sections.id).nullable().index()
}

// Locations you can take classes in at NYU
object Locations : IntIdTable() {
    val abbrev = varchar("abbreviation", length = 10).uniqueIndex()
    val name = varchar("name", length = 100)
}

// A class meeting
object Meetings : IntIdTable() {
    val sectionId = reference("section_id", Sections.id).index()
    val locationId = reference("location_id", Locations.id)
    val instructor = varchar("instructor", length = 50)
    val date = datetime("start")
    val duration = integer("duration") // Duration of event in minutes
    val repeat =
        integer("repeat")  // Repeat time, in minutes after the last one ended. Non-positive values mean no repeat
}

