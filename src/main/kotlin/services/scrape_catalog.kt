package services

// @CodeOrg This file should be in java/scraping

import scraping.models.Course
import nyu.SubjectCode
import nyu.Term
import mu.KotlinLogging

private val scraperLogger = KotlinLogging.logger("services.scrape_catalog")

/**
 * Scraping the catalogs from Albert Mobile given multiple subjecs
 * @param term The term for which we should be scraping
 * @param subjectCodes The subjects for which we should be scraping
 * @return Sequence of List of Courses
 */
fun scrapeFromCatalog(term: Term, subjectCodes: List<SubjectCode>,
                      batchSize: Int? = null): Sequence<List<Course>> {
    return queryCatalog(term, subjectCodes, batchSize).map { rawData ->
        try {
            ParseCatalog.parse(rawData.data, rawData.subject)
        } catch (e: Exception) {
            scraperLogger.warn { e.message }
            null
        }
    }.filterNotNull()
}

/**
 * Scraping the catalogs from Albert Mobile given one subject code
 * @param term The term for which we should be scraping
 * @param subjectCodes The subject for which we should be scraping
 * @return List of courses
 */
fun scrapeFromCatalog(term: Term, subjectCode: SubjectCode): List<Course> {
    return queryCatalog(term, subjectCode).let { rawData ->
        ParseCatalog.parse(rawData.data, rawData.subject)
    }
}
