import database.Tables
import database.writeToDb
import models.School
import models.Subject
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
    val school = School("UA")
    val subject = Subject("BIOL", school)
    val rawData = queryCatalog(term, school, subject)

    val parsedData = parseCatalog(rawData, logger)

    for (e in parsedData) {
        e.writeToDb(term)
    }
}
