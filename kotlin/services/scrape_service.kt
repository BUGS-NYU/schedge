package services

import models.CatalogEntry
import models.Subject
import models.Term
import mu.KLogger

fun scrapeCatalog(logger: KLogger, term: Term, subjects: List<Subject>): Sequence<CatalogEntry> {
    return queryCatalog(logger, term, subjects.toTypedArray()).map { rawData ->
            parseCatalog(logger, rawData)
    }.flatten()
}

fun scrapeAll(logger: KLogger, term: Term, forSchool: String?): Sequence<CatalogEntry> =
    scrapeCatalog(logger, term, Subject.allSubjects(forSchool).toList())