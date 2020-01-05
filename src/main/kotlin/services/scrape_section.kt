package services

import scraping.models.Course
import models.SubjectCode
import models.Term
import mu.KLogger
import scraping.models.SectionAttribute
import java.lang.Exception


/**
 * Scraping the catalogs from Albert Mobile given one registration number
 * @param logger The logger to log to during execution of this service
 * @param term The term for which we should be scraping
 * @param subjectCodes The subject for which we should be scraping
 * @return List of courses
 */
fun scrapeFromSection(logger: KLogger, term: Term, registrationNumber: Int): SectionAttribute {
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
fun scrapeFromCatalogSection(logger: KLogger, term: Term, subjectCode: SubjectCode): Sequence<SectionAttribute> {
    return querySection(term, queryCatalog(term, subjectCode).let { rawData ->
        ParseCatalog.parseRegistrationNumber(logger, rawData)
    }).asSequence().map { rawData ->
        try {
            ParseSection.parse(rawData)
        } catch (e : Exception){
            logger.warn(e.message)
            null
        }
    }.filterNotNull()
}