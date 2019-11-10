package database

import models.SectionType
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.dao.LongIdTable

// All tables in the Schema
val Tables = arrayOf(Courses, Sections, Meetings)

// Courses that you can take at NYU
// Contains information like "MATH-UA 120" and "Discrete Math"
object Courses : LongIdTable() {
    val nyuCourseId = long("course_id")
    val name = varchar("name", length = 128)
    val subject = varchar("subject", length = 6).index()
    val school = varchar("school", length = 4).index()
    val deptCourseNumber = long("dept_course_number")
    val termId = integer("term_id").index()
}

// Sections that you can register for at NYU
object Sections : LongIdTable() {
    val registrationNumber = integer("registration_number").index()
    val courseId = reference("course_id", Courses).index()
    val sectionCode = varchar("section_code", length = 10)
    val instructor = text("instructor")
    val type = enumeration("type", klass = SectionType::class)
    val associatedWith = (long("associated_with").references(id)).nullable().index()
}

// A class meeting
object Meetings : LongIdTable() {
    val sectionId = reference("section_id", Sections).index()
    val start = datetime("start")
    val end = datetime("end")
    val duration = long("duration") // Duration of event in minutes
}

