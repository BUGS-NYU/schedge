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

fun parseCatalog(logger: KLogger, inputData: String): List<CatalogEntry> {
    return ParseCatalog.parse(parseHtml(logger, inputData))
}