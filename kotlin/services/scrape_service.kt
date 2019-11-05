package services

import models.Course
import models.Subject
import models.Term
import mu.KLogger

fun scrapeFromCatalog(logger: KLogger, term: Term, subjects: List<Subject>): Sequence<Course> {
    return queryCatalog(logger, term, subjects.toTypedArray()).map { (subject, rawData) ->
        parseCatalog(logger, rawData)
    }.flatten()
}

fun scrapeAllFromCatalog(logger: KLogger, term: Term, forSchool: String?): Sequence<Course> =
    scrapeFromCatalog(logger, term, Subject.allSubjects(forSchool).toList())
