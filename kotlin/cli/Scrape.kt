package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
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
    private val subjects by argument().multiple(required = false)
    private val allSubjects by option("--all-subjects").flag(default = false)

    override fun run() {
        connectToDatabase(logger)
        if (allSubjects) {
            require(subjects.isEmpty()) { "Can't provide subjects if '--all-subjects' flag is passed" }
            scrapeCatalog(logger, term, Subject.allSubjects().toList())
        } else {
            require(subjects.isNotEmpty()) { "Need to provide at least one argument" }
            scrapeCatalog(logger, term, subjects.map { string ->
                val (subject, school) = string.split('-')
                Subject(subject, school)
            })
        }

    }
}
