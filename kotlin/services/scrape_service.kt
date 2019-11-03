package services

import models.Course
import models.Subject
import models.Term
import mu.KLogger

fun scrapeCatalog(logger: KLogger, term: Term, vararg subjects: Subject): Sequence<Course> {
    return queryCatalog(logger, term, subjects).map { rawData ->
        parseCatalog(logger, rawData)
    }.flatten().map { it.toCourse() }
}

fun scrapeCatalog(logger: KLogger, term: Term, subjects: List<Subject>): Sequence<Course> {
    return scrapeCatalog(logger, term, *subjects.toTypedArray())
}