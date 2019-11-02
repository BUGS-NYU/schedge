package services

import database.Tables
import database.writeToDb
import models.Subject
import models.Term
import mu.KLogger
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun scrapeCatalog(logger: KLogger, term: Term, subjects: List<Subject>) {
    queryCatalog(logger, term, subjects.toTypedArray()).forEach { rawData ->
        // try {
            val parsedData = parseCatalog(logger, rawData)
            parsedData.forEach {
                it.writeToDb(term)
            }
        // } catch (e: Exception) {
        //     logger.error { "Error: $e" }
        // }
    }
}

fun scrapeAll(logger: KLogger, term: Term, forSchool: String?) {
    scrapeCatalog(logger, term, Subject.allSubjects(forSchool).toList())
}
