package cli

import com.github.ajalt.clikt.core.CliktCommand
import database.connectToDatabase
import models.Semester
import models.Term
import mu.KotlinLogging
import scrapeCatalog

class Scrape : CliktCommand(name = "scrape") {
    private val logger = KotlinLogging.logger("scrape")
    override fun run() {
        connectToDatabase(logger)
        scrapeCatalog(Term(Semester.Fall,2019), logger)
    }
}
