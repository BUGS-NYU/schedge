package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import models.SubjectCode
import models.Term
import mu.KotlinLogging
import services.JsonMapper
import services.scrapeFromCatalog
import utils.writeToFileOrStdout

// TODO Change this to package-level protected if that becomes a thing
/**
 * CLI for querying NYU Albert. Has two subcommands, `catalog` and `section`.
 * @author Albert Liu
 */
internal class Scrape : CliktCommand(name = "scrape") {

    init {
        this.subcommands(Catalog())
    }

    override fun run() = Unit

    /**
     * CLI for performing search queries of NYU Albert.
     */
    private class Catalog() : CliktCommand(name = "catalog") {
        private val logger = KotlinLogging.logger("scrape.catalog")
        private val term: Term by option("--term").convert {
            Term.fromId(Integer.parseInt(it))
        }.required()
        private val school: String by option("--school").required()
        private val subject: String? by option("--subject")
        private val file: String? by option("--file")
        private val prettyPrint by option("--pretty").flag(default = false)

        override fun run() {
            val startTime = System.nanoTime()
            if (subject == null) {
                file.writeToFileOrStdout(
                    JsonMapper.toJson(
                        scrapeFromCatalog(
                            logger,
                            term,
                            SubjectCode.allSubjects(school).toList()
                        ).toList(), prettyPrint
                    )
                )
            } else {
                file.writeToFileOrStdout(
                    JsonMapper.toJson(scrapeFromCatalog(logger, term, SubjectCode(subject!!, school)), prettyPrint)
                )
            }

            val endTime = System.nanoTime()
            val duration = (endTime - startTime) / 1000000000.0 //divide by 1000000 to get milliseconds.
            println("$duration seconds")

        }

    }
}
