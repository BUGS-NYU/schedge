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
    val abbrev = varchar("abbreviation", 10) // Column<String>
    val name = varchar("name", 100) // Column<String>
    val description = text("description")
}

object Sections : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val registrationNumber = integer("registration_number")
    val courseId = integer("courseId") references Courses.id
}

object Times : Table() {
    val id = integer("id").autoIncrement().primaryKey() // Column<Int>
    val timeOfDay = integer("time_of_day") // time in minutes
    val duration = integer("duration")
    val dayOfWeek = integer("day_of_week")
    val weekly = bool("weekly")
}

