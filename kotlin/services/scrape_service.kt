package services

import models.Course
import models.SubjectCode
import models.Term
import mu.KLogger
/**
Scraping the catalogs from Albert Mobile given multiple subjecs
@param Logger
@param Term (class)
@param subjectCodes (list of subject code)
@return Sequence of List of Courses
 */
fun scrapeFromCatalog(logger: KLogger, term: Term, subjectCodes: List<SubjectCode>): Sequence<List<Course>> {
    return queryCatalog(logger, term, subjectCodes).asSequence().map { rawData ->
        ParseCatalog.parse(logger, rawData)
    }
}
/**
Scraping the catalogs from Albert Mobile given one subject code
@param Logger
@param Term (class)
@param subjectCodes (list of subject code)
@return List of courses
 */
fun scrapeFromCatalog(logger: KLogger, term: Term, subjectCode: SubjectCode): List<Course> {
    return queryCatalog(logger, term, subjectCode).let { rawData ->
        ParseCatalog.parse(logger, rawData)
    }
}
/**
Scraping all catalogs from Albert Mobile given multiple subjecs
@param Logger
@param Term (class)
@param school's name (String)
@return Sequence of List of Courses
 */
fun scrapeAllFromCatalog(logger: KLogger, term: Term, forSchool: String?): Sequence<List<Course>> =
    scrapeFromCatalog(logger, term, SubjectCode.allSubjects(forSchool).toList())
