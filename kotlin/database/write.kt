package database

import models.CatalogEntry
import models.Term
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

fun CatalogEntry.writeToDb(term: Term) { // Perform an upsert
    val entry = this
    val termIdInt = term.id
    val courseEntityId = EntityID(entry.courseId * 10000 + termIdInt, Courses)

    val res = transaction {
        Courses.insert {
            it[id] = courseEntityId
            it[courseId] = entry.courseId
            it[termId] = termIdInt
            it[name] = entry.courseName
            it[subject] = entry.subject
            it[deptCourseNumber] = entry.deptCourseNumber
        }.resultedValues
    }

    println(res)
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
