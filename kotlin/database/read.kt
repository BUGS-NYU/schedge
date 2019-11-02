package database

import models.Course
import models.Subject
import models.Term
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun Course.Companion.getSubjects(
    term: Term,
    school: String
): Map<Subject, Array<Course>> {
    return Subject.allSubjects(school).map {
        Pair(it, getCourses(term, it))
    }.toMap()
}

fun Course.Companion.getCourses(term: Term, subject: Subject): Array<Course> {
    println(term.id)
    println(subject.abbrev)
    transaction {
        val results = Courses.join(Sections, joinType = JoinType.INNER) {
            Courses.id eq Sections.courseId
        }.join(Meetings, joinType = JoinType.INNER) {
            Sections.id eq Meetings.sectionId
        }.select {
            (Courses.termId eq term.id) and (Courses.subject eq subject.abbrev)
        }.orderBy(
            Pair(Courses.courseId, SortOrder.ASC),
            Pair(Sections.sectionNumber, SortOrder.ASC)
        )

        val sectionResults = results.asSequence().groupBy {
            Triple(
                it[Courses.courseId],
                it[Courses.name],
                it[Courses.deptCourseNumber]
            )
        }

        println(results)
    }

    return arrayOf()
}
