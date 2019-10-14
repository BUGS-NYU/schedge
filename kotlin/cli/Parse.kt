package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import mu.KLogger
import mu.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import parse.ParseCatalog
import parse.ParseSection
import readFromFileOrStdin
import writeToFileOrStdout
import java.io.IOException

// TODO Change this to package-level protected if that becomes a thing
internal class Parse : CliktCommand(name = "parse") {

    companion object {
        fun parseHtml(text: String, logger: KLogger): Document {
            return Jsoup.parse(text).let {
                if (it == null) {
                    logger.error {
                        "Jsoup returned a null for provided input ${text}."
                    }
                    throw IOException("Couldn't parse input!")
                }
                it
            }
        }
    }

    init {
        this.subcommands(Catalog(), Section())
    }

    override fun run() = Unit

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
            val document = parseHtml(input, logger)
            outputFile.writeToFileOrStdout(ParseCatalog.parse(document))
        }
    }

    private class Section : CliktCommand(name = "section") {
        private val logger = KotlinLogging.logger("parse.section")
        private val inputFile by argument(help = "The file to read from. If not provided, reads from stdin.")
        private val outputFile by option(
            "--output-file",
            help = "The file to write to. If not provided, writes to stdout."
        )
        // TODO Add this from Parser.parseSection
        override fun run() {
            val input = inputFile.readFromFileOrStdin()
            val document = parseHtml(input, logger)
            outputFile.writeToFileOrStdout(ParseSection.parse(document))
        }
    }
}