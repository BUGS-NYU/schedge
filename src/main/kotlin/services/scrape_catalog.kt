package services

// @CodeOrg This file should be in java/scraping

import scraping.models.Course
import models.SubjectCode
import models.Term
import mu.KotlinLogging

private val scraperLogger = KotlinLogging.logger("services.scrape_catalog")
/**
 * Scraping the catalogs from Albert Mobile given multiple subjecs
 * @param logger The logger to log to during execution of this service
 * @param term The term for which we should be scraping
 * @param subjectCodes The subjects for which we should be scraping
 * @return Sequence of List of Courses
 */
fun scrapeFromCatalog(term: Term, subjectCodes: List<SubjectCode>, batchSize: Int? = null): Sequence<List<Course>> {
    return queryCatalog(term, subjectCodes, batchSize).asSequence().map { rawData ->
      try {
          ParseCatalog.parse(scraperLogger, rawData)
      } catch (e: Exception) {
          scraperLogger.warn(e.message)
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
fun scrapeFromCatalog(term: Term, subjectCode: SubjectCode): List<Course> {
    return queryCatalog(term, subjectCode).let { rawData ->
        ParseCatalog.parse(scraperLogger, rawData)
    }
}

/**
 * Scraping all catalogs from Albert Mobile given multiple subjecs
 * @param logger The logger to log to during execution of this service
 * @param term The term for which we should be scraping
 * @param forSchool School's name
 * @return Sequence of List of Courses
 */
fun scrapeAllFromCatalog(term: Term, forSchool: String?, batchSize: Int? = null): Sequence<List<Course>> =
    scrapeFromCatalog(term, SubjectCode.allSubjects(forSchool).toList(), batchSize)
