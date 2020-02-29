package services

import nyu.SubjectCode
import nyu.Term
import scraping.models.SectionAttribute
import mu.KotlinLogging
import java.util.stream.Collectors
import java.util.stream.Stream

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
 * Scraping the catalogs' sections from Albert Mobile given multiple subject codes
 * @param term The term for which we should be scraping
 * @param subjectCodes The subject for which we should be scraping
 * @return List of courses
 */
fun scrapeFromAllCatalogSection(term: Term, subjectCodes: List<SubjectCode>,
                                batchSize: Int? = null): Stream<SectionAttribute> {
    return querySections(term,
            ParseCatalog.parseRegistrationNumber(
                    queryCatalog(term, subjectCodes).collect(Collectors.toList()).toString()), batchSize)
            .asIterable().map { rawData ->
                ParseSection.parse(rawData)
            }.stream()

}