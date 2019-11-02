package models

import database.Courses
import database.Sections
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

data class Course private constructor(
    val courseId: Long,
    val name: String,
    val deptCourseNumber: Int,
    val sections: Array<Section>
) {

    companion object {
        fun getSubjects(
            term: Term,
            school: String
        ): Map<Subject, Array<Course>> {
            return Subject.allSubjects(school).map {
                Pair(it, getCourses(term, it))
            }.toMap()
        }

        fun getCourses(term: Term, subject: Subject): Array<Course> {
            transaction {
                val courseResults = Courses.select {
                    (Courses.termId eq term.id) and (Courses.subject eq subject.abbrev)
                }

                courseResults.map {
                    val courseId = it[Courses.courseId]
                    val deptCourseNumber = it[Courses.deptCourseNumber]
                    val name = it[Courses.name]

                }
            }

            return arrayOf()
        }
    }

    override fun equals(other: Any?): Boolean { // AUTO GENERATED
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Course

        if (courseId != other.courseId) return false
        if (name != other.name) return false
        if (deptCourseNumber != other.deptCourseNumber) return false
        if (!sections.contentEquals(other.sections)) return false

        return true
    }

    override fun hashCode(): Int { // AUTO GENERATED
        var result = courseId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + deptCourseNumber
        result = 31 * result + sections.contentHashCode()
        return result
    }
}

sealed class Section private constructor(
    val registrationNumber: Int,
    val sectionNumber: Int,
    val instructor: String,
    val type: SectionType,
    val meetings: Array<Meeting>
) {

    class Lecture(
        registrationNumber: Int,
        sectionNumber: Int,
        instructor: String,
        meetings: Array<Meeting>,
        val recitations: Array<Recitation>?
    ) : Section(
        registrationNumber, sectionNumber, instructor, SectionType.LEC, meetings
    )

    class Recitation(
        registrationNumber: Int,
        sectionNumber: Int,
        instructor: String,
        meetings: Array<Meeting>
    ) : Section(
        registrationNumber, sectionNumber, instructor, SectionType.RCT, meetings
    )

}


