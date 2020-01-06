package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import models.SubjectCode
import models.Term
import mu.KotlinLogging
import scraping.models.Course
import services.*
import utils.writeToFileOrStdout

// TODO Change this to package-level protected if that becomes a thing
/**
 * CLI for querying NYU Albert. Has three subcommands, `catalog` and `section` and 'sections'.
 * @author Albert Liu
 */
internal class Scrape : CliktCommand(name = "scrape") {

    init {
        this.subcommands(Catalog(), Section(), Sections())
    }

    override fun run() = Unit

    /**
     * CLI for performing search queries for catalog's section of NYU Albert based registration number.
     */
    private class Section() : CliktCommand(name = "section") {
        private val logger = KotlinLogging.logger("scrape.section")
        private val term: Term by option("--term").convert {
            Term.fromId(Integer.parseInt(it))
        }.required()
        private val registrationNumber: Int by option(
                "--registrationNumber",
                help = "The registration number that you'd use to register for the course."
        ).int().required()
        private val file: String? by option("--file")
        private val prettyPrint by option("--pretty").flag(default = false)

        override fun run() {
            val startTime = System.nanoTime()
            file.writeToFileOrStdout(
                    JsonMapper.toJson(
                            scrapeFromSection(term, registrationNumber), prettyPrint)
            )

            val endTime = System.nanoTime()
            val duration = (endTime - startTime) / 1000000000.0 //divide by 1000000 to get milliseconds.
            logger.info { "$duration seconds" }

        }
    }

    /**
     * CLI for performing search queries for catalog's sections of NYU Albert based on school and subject.
     */
    private class Sections() : CliktCommand(name = "sections") {
        private val logger = KotlinLogging.logger("scrape.catalog")
        private val term: Term by option("--term").convert {
            Term.fromId(Integer.parseInt(it))
        }.required()
        private val school: String? by option("--school")
        private val subject: String? by option("--subject")
        private val file: String? by option("--file")
        private val batchSize: Int? by option("--batchSize").int()
        private val prettyPrint by option("--pretty").flag(default = false)

        override fun run() {
            val startTime = System.nanoTime()
            val school = school
            val subject = subject
            if(school != null && subject != null) {
                file.writeToFileOrStdout(
                        JsonMapper.toJson(
                                scrapeFromCatalogSection(logger, term, SubjectCode(subject, school), batchSize).toList(),
                                prettyPrint)
                )
            } else if(subject == null) {
                file.writeToFileOrStdout(
                        JsonMapper.toJson(
                                scrapeFromCatalogSection(logger, term, school, batchSize).toList(),
                                prettyPrint
                        )
                )
            } else {
                file.writeToFileOrStdout(
                        JsonMapper.toJson(
                                scrapeFromAllCatalogSection(logger, term, SubjectCode.allSubjects().toList(), batchSize),
                                prettyPrint
                        )
                )
            }
            val endTime = System.nanoTime()
            val duration = (endTime - startTime) / 1000000000.0 //divide by 1000000 to get milliseconds.
            logger.info { "$duration seconds" }
        }

    }



    /**
     * CLI for performing search queries of NYU Albert.
     */
    private class Catalog() : CliktCommand(name = "catalog") {
        private val logger = KotlinLogging.logger("scrape.catalog")
        private val term: Term by option("--term").convert {
            Term.fromId(Integer.parseInt(it))
        }.required()
        private val school: String? by option("--school")
        private val subject: String? by option("--subject")
        private val file: String? by option("--file")
        private val batchSize: Int? by option("--batchSize").int()
        private val prettyPrint by option("--pretty").flag(default = false)

        override fun run() {
            val startTime = System.nanoTime()
            val school = school
            val subject = subject
            if (school == null) {
                require(subject == null) { "--subject doesn't make sense if no school is provided." }
                file.writeToFileOrStdout(
                    JsonMapper.toJson(
                        scrapeFromCatalog(logger, term,
                            SubjectCode.allSubjects().toList(), batchSize
                        ).toList(), prettyPrint
                    )
                )
            } else if (subject == null) {
                file.writeToFileOrStdout(
                    JsonMapper.toJson(
                        scrapeAllFromCatalog(logger, term, school, batchSize).toList(), prettyPrint
                    )
                )
            } else {
                require( batchSize == null) { "Batch size doesn't make sense when only doing one query." }
                file.writeToFileOrStdout(
                    JsonMapper.toJson(
                      scrapeFromCatalog(logger, term, SubjectCode(subject, school)), prettyPrint)
                )
            }

            val endTime = System.nanoTime()
            val duration = (endTime - startTime) / 1000000000.0 //divide by 1000000 to get milliseconds.
            logger.info { "$duration seconds" }

        }

    }
}
