package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import database.Migrations
import database.Tables
import database.connectToDatabase
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

internal class Schedge() : CliktCommand(name = "schedge") {

    init {
        this.subcommands(Parse(), Query())
    }
    private val logger = KotlinLogging.logger {}

    override fun run() {
        connectToDatabase(logger)
        transaction {
            // TODO Handle migrations instead of just adding missing tables and columns
            SchemaUtils.createMissingTablesAndColumns(Migrations)
            SchemaUtils.createMissingTablesAndColumns(*Tables)
        }

        println(models.Subjects)
        println(models.Schools)
        logger.error("Nothing's been implemented!")
    }
}


