package services

import models.CatalogEntry
import models.Subject
import models.Term
import mu.KLogger

fun scrapeFromCatalog(logger: KLogger, term: Term, subjects: List<Subject>): Sequence<CatalogEntry> {
    return queryCatalog(logger, term, subjects.toTypedArray()).map { rawData ->
            parseCatalog(logger, rawData)
    }.flatten()
}

fun scrapeAllFromCatalog(logger: KLogger, term: Term, forSchool: String?): Sequence<CatalogEntry> =
    scrapeFromCatalog(logger, term, Subject.allSubjects(forSchool).toList())
