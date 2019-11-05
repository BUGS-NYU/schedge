package services

import models.Course
import mu.KLogger
import org.jsoup.Jsoup
import parse.ParseCatalog
import java.io.IOException

/** Parses the HTML of the catalog and returns a list of catalog entries.
 *
 * @param logger The logger that this service should log to
 * @param inputData The data that this service should parse
 *
 * @author Albert Liu
 */
fun parseCatalog(logger: KLogger, inputData: String): List<Course> {
    return Jsoup.parse(inputData).let {
        if (it == null) {
            logger.error { "Jsoup returned a null for provided input ${inputData}." }
            throw IOException("Couldn't parse input!")
        }
        it
    }.let { ParseCatalog.parse(it).asSequence().toList() }
}
