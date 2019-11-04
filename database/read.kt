package database

import Tuple
import mapFirst
import mapSecond
import models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun getSubjects(
    term: Term,
    school: String
): Map<Subject, Array<Course>> {
    return Subject.allSubjects(school).map {
        Pair(it, getCourses(term, it))
    }.toMap()
}

fun getCourses(term: Term, subject: Subject): Array<Course> {
    val meetingGroupedResults = transaction {
        val results = Courses.join(Sections, joinType = JoinType.INNER) {
            Courses.id eq Sections.courseId
        }.join(Meetings, joinType = JoinType.INNER) {
            Sections.id eq Meetings.sectionId
        }.select {
            (Courses.termId eq term.id) and (Courses.subject eq subject.abbrev)
        }

        // Turn it into Map<Pair<Long?, LabelledCourseSectionRow>, List<Meeting>>
        results.asSequence().groupBy(
            { Pair(it[Sections.associatedWith], LabelledCourseSectionRow.fromResultRow(it)) },
            Meeting.Companion::fromResultRow
        )
    }

    val (unassociatedResults, associatedResults) = meetingGroupedResults.asSequence().partition {
        it.key.first == null
    }.mapFirst { first ->
        first.map { Pair(it.key.second.sectionId, Pair(it.key.second, it.value)) }
            .toMap() // Make first into Map<Long, Pair<LabelledCourseSectionRow, List<Meeting>>>
    }.mapSecond { second ->
        second.map {
            val associatedWith = it.key.first
            requireNotNull(associatedWith) { "Partition fucked up bro" }
            val courseSectionRow = it.key.second
            val meetings = it.value
            Pair(associatedWith, courseSectionRow.toAssociatedSection(meetings))
        }.groupBy(
            { it.first },
            { it.second }
        ) // Make second into Map<Long, List<Section>>
    }

    return unassociatedResults.map {
        val associatedSections = associatedResults[it.key]
        val (courseSectionRow, meetings) = Pair(it.value.first, it.value.second)
        Pair(courseSectionRow.getCourseRow(), courseSectionRow.toSection(meetings, associatedSections))
    }.groupBy(
        { it.first },
        { it.second }
    ).map {
        val courseRow = it.key
        val sections = it.value
        Course(
            nyuCourseId = courseRow.nyuCourseId,
            name = courseRow.name,
            deptCourseNumber = courseRow.deptCourseNumber,
            sections = sections
        )
    }.toTypedArray()
}

data class CourseRow(
    val nyuCourseId: Long,
    val name: String,
    val deptCourseNumber: Long
)

data class LabelledCourseSectionRow(
    val sectionId: Long,
    val nyuCourseId: Long,
    val name: String,
    val deptCourseNumber: Long,
    val registrationNumber: Int,
    val sectionNumber: Int,
    val instructor: String,
    val type: SectionType
) {
    companion object {
        fun fromResultRow(row: ResultRow): LabelledCourseSectionRow {
            return LabelledCourseSectionRow(
                row[Sections.id].value,
                row[Courses.nyuCourseId],
                row[Courses.name],
                row[Courses.deptCourseNumber],
                row[Sections.registrationNumber],
                row[Sections.sectionNumber],
                row[Sections.instructor],
                row[Sections.type]
            )
        }
    }

    fun getCourseRow(): CourseRow {
        return CourseRow(
            nyuCourseId = nyuCourseId,
            name = name,
            deptCourseNumber = deptCourseNumber
        )
    }

    fun toSection(meetings: List<Meeting>, recitations: List<Section>?): Section {
        return Section.getSection(
            registrationNumber = registrationNumber,
            sectionNumber = sectionNumber,
            instructor = instructor,
            type = type,
            meetings = meetings,
            recitations = recitations
        )
    }

    fun toAssociatedSection(meetings: List<Meeting>): Section {
        return Section.getSection(
            registrationNumber = registrationNumber,
            sectionNumber = sectionNumber,
            instructor = instructor,
            type = type,
            meetings = meetings,
            recitations = null
        )
    }
}

data class AssociatedCourseSectionRow(
    val nyuCourseId: Long,
    val name: String,
    val deptCourseNumber: Long,
    val registrationNumber: Int,
    val sectionNumber: Int,
    val instructor: String,
    val type: SectionType,
    val associatedWith: Long
) {
    companion object {
        fun fromResultRow(row: ResultRow): AssociatedCourseSectionRow {
            val associatedWith = row[Sections.associatedWith]
            requireNotNull(associatedWith)
            return AssociatedCourseSectionRow(
                row[Courses.nyuCourseId],
                row[Courses.name],
                row[Courses.deptCourseNumber],
                row[Sections.registrationNumber],
                row[Sections.sectionNumber],
                row[Sections.instructor],
                row[Sections.type],
                associatedWith
            )
        }
    }
}

fun Meeting.Companion.fromResultRow(row: ResultRow): Meeting {
    return Meeting(row[Meetings.date], row[Meetings.duration], row[Meetings.activeDuration])
}
