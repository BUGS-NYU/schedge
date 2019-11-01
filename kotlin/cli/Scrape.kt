package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import database.connectToDatabase
import models.Subject
import models.Term
import mu.KotlinLogging
import services.scrapeCatalog

class Scrape : CliktCommand(name = "scrape") {
    private val logger = KotlinLogging.logger("scrape")
    private val term by option("--term").convert { Term.fromId(it.toInt()) }.required()
    private val subjects by argument().multiple(required = true)

    override fun run() {
        connectToDatabase(logger)
        scrapeCatalog(logger, term, subjects.map { string ->
            val (subject, school) = string.split('-')
            Subject(subject, school)
        })
    }
}
