package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import mu.KotlinLogging
import readFromFileOrStdin
import services.ParseCatalog
import writeToFileOrStdout

// TODO Change this to package-level protected if that becomes a thing
/**
 * CLI for parsing information gotten from NYU Albert. Has two subcommands, `catalog`
 * and `section`.
 * @author Albert Liu
 */
internal class Parse : CliktCommand(name = "parse") {

    init {
        this.subcommands(Catalog())
    }

    override fun run() = Unit

    /**
     * CLI for parsing data gotten from a single search query of NYU Albert.
     */
    private class Catalog : CliktCommand(name = "catalog") {
        private val logger = KotlinLogging.logger("parse.catalog")
        private val inputFile by argument(help = "The file to read from. If not provided, reads from stdin.")
        private val outputFile by option(
            "--output-file",
            help = "The file to write to. If not provided, writes to stdout."
        )

        // TODO Add this from Parser.parseCourse
        override fun run() {
            val input = inputFile.readFromFileOrStdin()
            val output = ParseCatalog.parse(logger, input)
            outputFile.writeToFileOrStdout(output)
        }
    }

//    private class Section : CliktCommand(name = "section") {
//        private val logger = KotlinLogging.logger("parse.section")
//        private val inputFile by argument(help = "The file to read from. If not provided, reads from stdin.")
//        private val outputFile by option(
//            "--output-file",
//            help = "The file to write to. If not provided, writes to stdout."
//        )
//
//        // TODO Add this from Parser.parseSection
//        override fun run() {
//            val input = inputFile.readFromFileOrStdin()
//            val document = parseHtml(input, logger)
//            outputFile.writeToFileOrStdout(ParseSection.parse(document))
//        }
//    }
}
