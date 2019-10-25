package database

import models.CatalogEntry
import models.CatalogSectionEntry
import models.Term
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.util.PSQLException

fun CatalogEntry.writeToDb(term: Term) { // Perform an upsert
    val entry = this
    val termIdInt = term.id
    val courseEntityId = EntityID(entry.courseId.toLong() * 10000 + termIdInt, Courses)

    transaction {
        val successful = try {
            Courses.insert {
                it[id] = courseEntityId
                it[courseId] = entry.courseId
                it[termId] = termIdInt
                it[name] = entry.courseName
                it[subject] = entry.subject
                it[deptCourseNumber] = entry.deptCourseNumber
            }
            true
        } catch (e: PSQLException) {
            false
        }
        if (successful) {
            val getSectionId = { section: CatalogSectionEntry ->
                EntityID(
                    section.sectionNumber.toLong() * 10000 + termIdInt,
                    Sections
                )
            }

            Sections.batchInsert(entry.sections) { section ->
                this[Sections.registrationNumber] = section.registrationNumber
                this[Sections.courseId] = courseEntityId
                this[Sections.id] = getSectionId(section)
                this[Sections.termId] = termIdInt
                this[Sections.sectionNumber] = section.sectionNumber
                this[Sections.associatedWith] = if (section.associatedWith != null) {
                    getSectionId(section.associatedWith)
                } else {
                    null
                }
            }

        }
    }

}

fun <T : Table> T.insertOrUpdate(
    vararg keys: Column<*>,
    body: T.(InsertStatement<Number>) -> Unit
) = InsertOrUpdate<Number>(keys, this).apply {
    body(this)
    execute(TransactionManager.current())
}

class InsertOrUpdate<Key : Any>(
    private val keys: Array<out Column<*>>,
    table: Table,
    isIgnore: Boolean = false
) : InsertStatement<Key>(table, isIgnore) {
    override fun prepareSQL(transaction: Transaction): String {
        val updateSetter = super.values.keys.joinToString {
            "${it.name} = EXCLUDED.${it.name}"
        }

        val keyColumns = keys.joinToString(",") { it.name }
        val onConflict = "ON CONFLICT ($keyColumns) DO UPDATE SET $updateSetter"
        return "${super.prepareSQL(transaction)} $onConflict"
    }
}
