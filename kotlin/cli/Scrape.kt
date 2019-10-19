package cli

import com.github.ajalt.clikt.core.CliktCommand
import database.connectToDatabase
import mu.KotlinLogging

class Scrape : CliktCommand(name = "scrape") {
    private val logger = KotlinLogging.logger("scrape")
    override fun run() {
        connectToDatabase(logger)
    }
}
