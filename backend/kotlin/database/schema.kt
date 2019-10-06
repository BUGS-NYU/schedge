package database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Courses : Table() {
    val id = long("id").autoIncrement().primaryKey() // Column<Int>
    val courseId = long("id") // Column<Long>
    val abbrev = varchar("abbreviation", 10) // Column<String>
    val name = varchar("name", 100) // Column<String>
}

