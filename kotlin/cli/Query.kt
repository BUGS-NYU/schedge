package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import models.Subject
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
        private val school: String by option("--school").required()
        private val subject: String by option("--subject").required()
        // private val catalogNumber: Int? by option("--catalog-number").int().restrictTo(0..Int.MAX_VALUE)
        // private val keywords: String? by option("--keywords")
        // private val classNumber: Int? by option("--class-number").int().restrictTo(0..Int.MAX_VALUE)
        // private val location: String? by option("--location")
        private val file: String? by option("--file")

        override fun run() =
            file.writeToFileOrStdout(
                queryCatalog(
                    KotlinLogging.logger("query.catalog"),
                    term = term,
                    subjects = arrayOf(Subject(subject, school))
                    // catalogNumber = catalogNumber,
                    // keywords = keywords,
                    // classNumber = classNumber,
                    // location = location
                )
            )

    }
}
