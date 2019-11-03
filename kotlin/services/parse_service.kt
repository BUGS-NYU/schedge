package services

import models.CatalogEntry
import mu.KLogger
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import parse.ParseCatalog
import java.io.IOException

private fun parseHtml(logger: KLogger, text: String): Document {
    return Jsoup.parse(text).let {
        if (it == null) {
            logger.error {
                "Jsoup returned a null for provided input ${text}."
            }
            throw IOException("Couldn't parse input!")
        }
        it
    }
}

/** Parses the HTML of the catalog and returns a list of catalog entries.
 *
 * @param logger The logger that this service should log to
 * @param inputData The data that this service should parse
 *
 * @author Albert Liu
 */
fun parseCatalog(logger: KLogger, inputData: String): Sequence<CatalogEntry> {
    val parsedHtml = parseHtml(logger, inputData)
    return ParseCatalog.parse(parsedHtml).asSequence()
}
