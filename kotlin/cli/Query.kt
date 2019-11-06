package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import models.SubjectCode
import models.Term
import mu.KotlinLogging
import services.queryCatalog
import writeToFileOrStdout

// TODO Change this to package-level protected if that becomes a thing
/**
 * CLI for querying NYU Albert. Has two subcommands, `catalog` and `section`.
 * @author Albert Liu
 */
internal class Query : CliktCommand(name = "query") {

    init {
        this.subcommands(Catalog())
    }

    override fun run() = Unit

    /**
     * CLI for performing search queries of NYU Albert.
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
                queryCatalog(logger, term, SubjectCode(subject, school)).asSequence().toList()
            )

    }
}
