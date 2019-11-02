package database

import models.CatalogEntry
import models.CatalogSectionEntry
import models.Term
import mu.KotlinLogging
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.postgresql.util.PSQLException

val logger = KotlinLogging.logger("database.write")

fun Iterable<CatalogEntry>.writeToDb(term: Term) {
    term.id
    TODO()
}

fun CatalogEntry.writeToDb(term: Term) { // Perform an upsert
    val entry = this
    val termIdInt = term.id
    val courseEntityId = EntityID(entry.courseId.toLong() * 10000 + termIdInt, Courses)
    val getSectionId = { section: CatalogSectionEntry ->
        EntityID(section.registrationNumber.toLong() * 10000 + termIdInt, Sections)
    }

    transaction {
        Courses.upsert(Courses.id) {
            it[id] = courseEntityId
            it[courseId] = entry.courseId
            it[termId] = termIdInt
            it[name] = entry.courseName
            it[subject] = entry.subject
            it[deptCourseNumber] = entry.deptCourseNumber
        }


        val meetings = entry.sections.map { section ->
            section.meetings.map { meeting -> Pair(getSectionId(section), meeting) }
        }.flatten()
        Sections.batchUpsert(entry.sections, Sections.id) { section ->
            this[Sections.registrationNumber] = section.registrationNumber
            this[Sections.courseId] = courseEntityId
            this[Sections.id] = getSectionId(section)
            this[Sections.termId] = termIdInt
            this[Sections.sectionNumber] = section.sectionNumber
            this[Sections.type] = section.type
            this[Sections.instructor] = section.instructor
            this[Sections.associatedWith] = if (section.associatedWith != null) {
                section.associatedWith.registrationNumber.toLong() * 10000 + termIdInt
            } else {
                null
            }

        }

        Meetings.batchUpsert(meetings, Meetings.id) { (section, meeting) ->
            this[Meetings.date] = DateTime(meeting.beginDate)
            this[Meetings.duration] = meeting.duration.millis
            this[Meetings.activeDuration] = meeting.activeDuration.millis
            this[Meetings.sectionId] = section
        }
    }

}

fun <T : Table> T.upsert(vararg keys: Column<*>, body: T.(InsertStatement<Number>) -> Unit) =
    Upsert<Number>(keys, this).apply {
        body(this)
        execute(TransactionManager.current())
    }

class Upsert<Key : Any>(
    private val keys: Array<out Column<*>>,
    table: Table,
    isIgnore: Boolean = false
) : InsertStatement<Key>(table, isIgnore) {
    override fun prepareSQL(transaction: Transaction): String {
        val updateSetter = super.values.keys.joinToString { "${it.name} = EXCLUDED.${it.name}" }
        require(keys.isNotEmpty()) { "Need to provide at least one conflict key!" }
        val keyColumns = keys.joinToString(",") { it.name }
        val onConflict = "ON CONFLICT ($keyColumns) DO UPDATE SET $updateSetter"
        return "${super.prepareSQL(transaction)} $onConflict"
    }
}

fun <T : Table, E : Any> T.batchUpsert(
    batchData: Iterable<E>,
    vararg keys: Column<*>,
    body: BatchInsertStatement.(E) -> Unit
) {
    val insertStatement = BatchUpsertStatement(keys, this)
    var count = 0
    batchData.forEach {
        insertStatement.addBatch()
        insertStatement.body(it)
        count += 1
    }

    if (count == 0) return // Nothing was inserted

    TransactionManager.current().exec(insertStatement)
}

class BatchUpsertStatement(
    private val keys: Array<out Column<*>>,
    table: Table,
    isIgnore: Boolean = false
) : BatchInsertStatement(table, isIgnore) {
    override fun prepareSQL(transaction: Transaction): String {
        val updateSetter = super.values.keys.joinToString { "${it.name} = EXCLUDED.${it.name}" }
        require(keys.isNotEmpty()) { "Need to provide at least one conflict key!" }
        val keyColumns = keys.joinToString(",") { it.name }
        val onConflict = "ON CONFLICT ($keyColumns) DO UPDATE SET $updateSetter"
        return "${super.prepareSQL(transaction)} $onConflict"
    }
}