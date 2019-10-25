import database.Tables
import database.writeToDb
import models.Schools
import models.Subjects
import models.Term
import mu.KLogger
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import services.parseCatalog
import services.queryCatalog

fun scrapeCatalog(term: Term, logger: KLogger) {
    transaction {
        SchemaUtils.createMissingTablesAndColumns(*Tables)
    }
    val rawData = queryCatalog(term,
        Schools["UA"] ?: error("Couldn't get school"),
        Subjects["BIOL-UA"] ?: error("Couldn't get subject")
    )

    val parsedData = parseCatalog(rawData, logger)

    for (e in parsedData) {
        e.writeToDb(term)
    }
}
