package services

import models.CatalogEntry
import models.Subject
import models.Term
import mu.KLogger

fun dbAddFromCatalog(logger: KLogger, term: Term, subjects: List<Subject>): Sequence<CatalogEntry> {
    return queryCatalog(logger, term, subjects.toTypedArray()).map { rawData ->
            parseCatalog(logger, rawData)
    }.flatten()
}

fun dbAddAllFromCatalog(logger: KLogger, term: Term, forSchool: String?): Sequence<CatalogEntry> =
    dbAddFromCatalog(logger, term, Subject.allSubjects(forSchool).toList())