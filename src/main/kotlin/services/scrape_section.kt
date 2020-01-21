package services

import scraping.models.Course
import models.SubjectCode
import models.Term
import mu.KLogger
import mu.KotlinLogging
import scraping.models.SectionAttribute
import scraping.models.Subject
import java.lang.Exception

private val scraperLogger = KotlinLogging.logger("services.scrape_section")

/**
 * Scraping the catalogs from Albert Mobile given one registration number
 * @param logger The logger to log to during execution of this service
 * @param term The term for which we should be scraping
 * @param subjectCodes The subject for which we should be scraping
 * @return List of courses
 */
fun scrapeFromSection(term: Term, registrationNumber: Int): SectionAttribute {
    return querySection(term, registrationNumber).let { rawData ->
        ParseSection.parse(rawData)
    }
}

/**
 * Scraping the catalogs' sections from Albert Mobile given one subject code
 * @param logger The logger to log to during execution of this service
 * @param term The term for which we should be scraping
 * @param subjectCodes The subject for which we should be scraping
 * @return List of courses
 */
fun scrapeFromCatalogSection(term: Term, subjectCode: SubjectCode, batchSize: Int? = null): Sequence<SectionAttribute> {
    return querySection(term, queryCatalog(term, subjectCode).let { rawData ->
        ParseCatalog.parseRegistrationNumber(scraperLogger, rawData)
    }, batchSize).asSequence().map { rawData ->
        try {
            ParseSection.parse(rawData)
        } catch (e : Exception){
            scraperLogger.warn(e.message)
            null
        }
    }.filterNotNull()
}

/**
 * Scraping the catalogs' sections from Albert Mobile given school name
 * @param logger The logger to log to during execution of this service
 * @param term The term for which we should be scraping
 * @param subjectCodes The subject for which we should be scraping
 * @return List of courses
 */
fun scrapeFromCatalogSection(term : Term, forSchool: String?, batchSize: Int? = null) : Sequence<SectionAttribute> {
    return querySection(term,
            ParseCatalog.parseRegistrationNumber(scraperLogger,
                    queryCatalog(term, SubjectCode.allSubjects(forSchool)).toList().toString()), batchSize)
            .asSequence().map { rawData ->
                ParseSection.parse(rawData)
            }
}

/**
 * Scraping the catalogs' sections from Albert Mobile given multiple subject codes
 * @param logger The logger to log to during execution of this service
 * @param term The term for which we should be scraping
 * @param subjectCodes The subject for which we should be scraping
 * @return List of courses
 */
fun scrapeFromAllCatalogSection(term: Term, subjectCodes : List<SubjectCode>, batchSize: Int? = null) : Sequence<SectionAttribute> {
    return querySection(term,
            ParseCatalog.parseRegistrationNumber(scraperLogger,
                    queryCatalog(term, subjectCodes).toList().toString()), batchSize)
            .asSequence().map { rawData ->
                ParseSection.parse(rawData)
            }

}