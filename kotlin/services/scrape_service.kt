package services

import database.Tables
import database.writeToDb
import models.Subject
import models.Term
import mu.KLogger
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun scrapeCatalog(logger: KLogger, term: Term, subjects: List<Subject>) {
    transaction {
        SchemaUtils.createMissingTablesAndColumns(*Tables)
    }

    queryCatalog(logger, term, subjects.toTypedArray()).forEach { rawData ->
        val parsedData = parseCatalog(logger, rawData)

        parsedData.forEach {
            it.writeToDb(term)
        }
    }
}
