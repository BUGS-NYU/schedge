package database

import org.jetbrains.exposed.sql.*

object Migrations : Table() {
    val id = integer("id").autoIncrement().primaryKey() // Column<Int>
    val dateAdded = datetime("date_added")
    val description = text("description")
}

object Courses : Table() {
    val id = integer("id").autoIncrement().primaryKey() // Column<Int>
    val courseId = integer("courseId") // Column<Long>
    val abbrev = varchar("abbreviation", length = 10) // Column<String>
    val name = varchar("name", length = 100) // Column<String>
    val description = text("description")
}

object Sections : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val registrationNumber = integer("registration_number")
    val courseId = integer("courseId") references Courses.id
    val type = varchar("type", length = 3)
    val associatedWith = (integer("associated_with") references id).nullable()
}

object Locations : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val abbrev = varchar("abbreviation", length = 10)
    val name = varchar("name", length = 100)
}

object Meetings : Table() {
    val id = integer("id").autoIncrement().primaryKey() // Column<Int>
    val sectionId = integer("section_id") references Sections.id
    val locationId = integer("location_id") references Locations.id
    val date = datetime("start")
    val duration = integer("duration") // Duration of event in minutes
    val repeat = integer("repeat")  // Repeat time, in minutes after the last
    // one ended. Non-positive values mean no repeat
}

