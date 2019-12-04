package cli

import api.App
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import models.SubjectCode
import models.Term
import mu.KotlinLogging
import services.*
import utils.writeToFileOrStdout

internal class Database : CliktCommand(name = "db") {

    init {
        this.subcommands(Scrape(), Query(), Serve())
    }

    override fun run() = Unit

    private class Scrape : CliktCommand(name = "scrape") {
        private val logger = KotlinLogging.logger("db.scrape")
        private val term: Term by option("--term").convert {
            Term.fromId(Integer.parseInt(it))
        }.required()
        private val school: String? by option("--school")
        private val subject: String? by option("--subject")

        override fun run() {
            val startTime = System.nanoTime()
            val school = school
            val subject = subject
            if (school == null) {
                scrapeFromCatalog(
                    logger, term, SubjectCode.allSubjects().toList()
                ).forEach {
                    InsertCourses.insertCourses(logger, term, it)
                }
            } else if (subject == null) {
                scrapeFromCatalog(
                    logger, term, SubjectCode.allSubjects(school).toList()
                ).forEach {
                    InsertCourses.insertCourses(logger, term, it)
                }
            } else {
                InsertCourses.insertCourses(
                    logger,
                    term,
                    scrapeFromCatalog(logger, term, SubjectCode(subject, school))
                )
            }
            GetConnection.close()

            val endTime = System.nanoTime()
            val duration = (endTime - startTime) / 1000000000.0 //divide by 1000000 to get milliseconds.
            logger.info { "$duration seconds" }
        }
    }

    private class Query : CliktCommand(name = "query") {
        private val logger = KotlinLogging.logger("db.query")
        private val term: Term by option("--term").convert {
            Term.fromId(Integer.parseInt(it))
        }.required()
        private val school: String by option("--school").required()
        private val subject: String? by option("--subject")
        private val prettyPrint by option("--pretty").flag(default = false)
        private val outputFile by option(
            "--output-file",
            help = "The file to write to. If not provided, writes to stdout."
        )

        override fun run() {
            val courses = if (subject == null) {
                SelectCourses.selectCourses(
                    logger, term, SubjectCode.allSubjects(school).asSequence().toMutableList()
                )
            } else {
                SelectCourses.selectCourses(
                    logger, term, SubjectCode(subject!!, school)
                )
            }
            GetConnection.close()

            outputFile.writeToFileOrStdout(JsonMapper.toJson(courses, prettyPrint));
        }
    }


    private class Serve : CliktCommand(name = "serve") {
        override fun run(): Unit = App.run()
    }

}
