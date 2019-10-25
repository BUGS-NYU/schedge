package cli

import com.github.ajalt.clikt.core.CliktCommand
import database.connectToDatabase
import models.School
import models.Semester
import models.Subject
import models.Term
import mu.KotlinLogging
import scrapeCatalog

class Scrape : CliktCommand(name = "scrape") {
    private val logger = KotlinLogging.logger("scrape")
    override fun run() {
        connectToDatabase(logger)
        val term = Term(Semester.Fall,2019)
        val school = School("UA")
        val subject = Subject("BIOL", school)
        scrapeCatalog(term, school, subject, logger)
    }
}
