package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import models.SubjectCode
import models.Term
import mu.KotlinLogging
import services.InsertCourses
import services.scrapeFromCatalog

internal class Database : CliktCommand(name = "db") {

    init {
        this.subcommands(Scrape())
    }

    override fun run() = Unit

    private class Scrape : CliktCommand(name = "scrape") {
        private val logger = KotlinLogging.logger("db.scrape")
        private val term: Term by option("--term").convert {
            Term.fromId(Integer.parseInt(it))
        }.required()
        private val school: String by option("--school").required()
        private val subject: String? by option("--subject")

        override fun run() {
            val startTime = System.nanoTime()
            if (subject == null) {
                InsertCourses.insertCourses(
                    logger, term, scrapeFromCatalog(
                        logger, term, SubjectCode.allSubjects(school).toList()
                    ).flatten().toMutableList()
                )
            } else {
                InsertCourses.insertCourses(
                    logger,
                    term,
                    scrapeFromCatalog(logger, term, SubjectCode(subject!!, school))
                )
            }

            val endTime = System.nanoTime()
            val duration = (endTime - startTime) / 1000000000.0 //divide by 1000000 to get milliseconds.
            println("$duration seconds")
        }

    }

}
