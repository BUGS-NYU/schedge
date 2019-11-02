package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.*
import database.Tables
import database.connectToDatabase
import models.Subject
import models.Term
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import services.scrapeAll
import services.scrapeCatalog

class Scrape : CliktCommand(name = "scrape") {
    private val logger = KotlinLogging.logger("scrape")
    private val term by option("--term").convert { Term.fromId(it.toInt()) }.required()
    private val subjects by argument().multiple(required = false)
    private val school by option("--school")
    private val allSubjects by option("--all-subjects").flag(default = false)

    override fun run() {
        connectToDatabase(logger)
        transaction {
            SchemaUtils.createMissingTablesAndColumns(*Tables)
        }
        if (allSubjects) {
            require(subjects.isEmpty()) { "Can't provide subjects if '--all-subjects' flag is passed" }
            require(school == null) { "Can't provide school if '--all-subjects' flag is passed" }
            scrapeAll(logger, term, school)
        } else if (school == null) {
            require(subjects.isNotEmpty()) { "Need to provide at least one argument" }
            scrapeCatalog(logger, term, subjects.map { string ->
                val (subject, school) = string.split('-')
                Subject(subject, school)
            })
        } else {
            require(subjects.isEmpty()) { "Can't provide subjects if '--school' flag is passed" }
            scrapeAll(logger, term, school)
        }

    }
}
