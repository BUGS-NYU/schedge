package services

import models.Course
import models.SubjectCode
import models.Term
import mu.KLogger

fun scrapeFromCatalog(logger: KLogger, term: Term, subjectCodes: List<SubjectCode>): Sequence<Pair<SubjectCode, List<Course>>> {
    return QueryCatalog.query(logger, term, subjectCodes).asSequence().map { (subject, rawData) ->
        Pair(subject, ParseCatalog.parse(logger, rawData))
    }
}

fun scrapeFromCatalog(logger: KLogger, term: Term, subjectCode: SubjectCode): List<Course> {
    return queryCatalog(logger, term, subjectCode).let { rawData ->
        ParseCatalog.parse(logger, rawData)
    }
}

fun scrapeAllFromCatalog(logger: KLogger, term: Term, forSchool: String?): Sequence<Pair<SubjectCode, List<Course>>> =
    scrapeFromCatalog(logger, term, SubjectCode.allSubjects(forSchool).toList())
