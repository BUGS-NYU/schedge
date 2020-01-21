package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import models.SubjectCode
import models.Term
import mu.KotlinLogging
import services.queryCatalog
import services.querySchool
import services.querySection
import utils.writeToFileOrStdout

// TODO Change this to package-level protected if that becomes a thing
/**
 * CLI for querying NYU Albert. Has two subcommands, `catalog` and `section`.
 * @author Albert Liu
 */
internal class Query : CliktCommand(name = "query") {

    init {
        this.subcommands(Catalog(), Section(), School())
    }

    override fun run() = Unit

    /**
     * CLI for performing search queries of NYU Albert for a single section.
     */
    private class Section() : CliktCommand(name = "section") {
        private val logger = KotlinLogging.logger("query.section")
        private val term: Term by option("--term").convert {
            Term.fromId(Integer.parseInt(it))
        }.required()
        private val registrationNumber: Int by option(
            "--registrationNumber",
            help = "The registration number that you'd use to register for the course."
        ).int().required()
        private val outputFile: String? by option(help = "The file to output to. If none provided, prints to stdout.")

        override fun run() =
            outputFile.writeToFileOrStdout(querySection(term, registrationNumber))
    }

    /**
     * CLI for performing search queries of NYU Albert's catalog.
     */
    private class Catalog() : CliktCommand(name = "catalog") {
        private val logger = KotlinLogging.logger("query.catalog")
        private val term: Term by option("--term").convert {
            Term.fromId(Integer.parseInt(it))
        }.required()
        private val school: String by option("--school", help = "The school to use in the query.").required()
        private val subject: String by option("--subject", help = "The subject to use in the query.").required()
        private val outputFile: String? by option(help = "The file to output to. If none provided, prints to stdout.")

        override fun run() =
            outputFile.writeToFileOrStdout(
                queryCatalog(term, SubjectCode(subject, school))
            )

    }

    private class School() : CliktCommand(name = "school") {
        private val term: Term by option("--term").convert {
            Term.fromId(Integer.parseInt(it))
        }.required()
        private val outputFile: String? by option(help = "The file to output to. If none provided, prints to stdout.")

        override fun run() =
                outputFile.writeToFileOrStdout(
                        querySchool(term)
                )

    }
}
