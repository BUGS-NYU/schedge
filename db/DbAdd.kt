package cli.db

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.*
import database.writeToDb
import models.Subject
import models.Term
import mu.KotlinLogging
import services.scrapeAllFromCatalog
import services.scrapeFromCatalog

internal class DbAdd : CliktCommand(name = "add") {
    private val logger = KotlinLogging.logger("db.add")
    private val term by option("--term").convert { Term.fromId(it.toInt()) }.required()
    private val subjects by argument().multiple(required = false)
    private val school by option("--school")
    private val allSubjects by option("--all-subjects").flag(default = false)

    override fun run() {

        if (allSubjects) {
            require(subjects.isEmpty()) { "Can't provide subjects if '--all-subjects' flag is passed" }
            require(school == null) { "Can't provide school if '--all-subjects' flag is passed" }
            scrapeAllFromCatalog(logger, term, null).forEach {
                it.writeToDb(term)
            }
        } else if (school == null) {
            require(subjects.isNotEmpty()) { "Need to provide at least one argument" }
            scrapeFromCatalog(logger, term, subjects.map { string ->
                val (subject, school) = string.split('-')
                Subject(subject, school)
            }).forEach {
                it.writeToDb(term)
            }
        } else {
            require(subjects.isEmpty()) { "Can't provide subjects if '--school' flag is passed" }
            scrapeAllFromCatalog(logger, term, school).forEach {
                it.writeToDb(term)
            }
        }

    }
}
