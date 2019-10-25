import database.writeToDb
import models.Schools
import models.Subjects
import models.Term
import mu.KLogger
import services.parseCatalog
import services.queryCatalog

fun scrapeCatalog(term: Term, logger: KLogger) {
    val rawData = queryCatalog(term,
        Schools["UA"] ?: error("Couldn't get school"),
        Subjects["MATH-UA"] ?: error("Couldn't get subject")
    )

    val parsedData = parseCatalog(rawData, logger)

    for (e in parsedData) {
        e.writeToDb(term)
    }
}
