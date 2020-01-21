package services

// @CodeOrg This file should be in java/scraping

import models.SubjectCode
import models.Term
import mu.KLogger
import scraping.models.Course

/**
 * Scraping the catalogs from Albert Mobile given multiple subjecs
 * @param logger The logger to log to during execution of this service
 * @param term The term for which we should be scraping
 * @param subjectCodes The subjects for which we should be scraping
 * @return Sequence of List of Courses
 */
fun scrapeFromCatalog(logger: KLogger, term: Term, subjectCodes: List<SubjectCode>, batchSize: Int? = null): Sequence<List<Course>> {
    return queryCatalog(term, subjectCodes, batchSize).asSequence().map { rawData ->
        try {
            ParseCatalog.parse(logger, rawData)
        } catch (e: Exception) {
            logger.warn(e.message)
            null
        }
    }.filterNotNull()
}

/**
 * Scraping the catalogs from Albert Mobile given one subject code
 * @param logger The logger to log to during execution of this service
 * @param term The term for which we should be scraping
 * @param subjectCodes The subject for which we should be scraping
 * @return List of courses
 */
fun scrapeFromCatalog(logger: KLogger, term: Term, subjectCode: SubjectCode): List<Course> {
    return queryCatalog(term, subjectCode).let { rawData ->
        ParseCatalog.parse(logger, rawData)
    }
}

/**
 * Scraping all catalogs from Albert Mobile given multiple subjecs
 * @param logger The logger to log to during execution of this service
 * @param term The term for which we should be scraping
 * @param forSchool School's name
 * @return Sequence of List of Courses
 */
fun scrapeAllFromCatalog(logger: KLogger, term: Term, forSchool: String?, batchSize: Int? = null): Sequence<List<Course>> =
        scrapeFromCatalog(logger, term, SubjectCode.allSubjects(forSchool).toList(), batchSize)
