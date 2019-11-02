package cli.db

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import database.Tables
import database.connectToDatabase
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

internal class Database : CliktCommand(name = "db") {

    init {
        this.subcommands(DbAdd(), DbGet(), Serve())
    }

    override fun run() {
        connectToDatabase(KotlinLogging.logger("db"))
        transaction {
            SchemaUtils.createMissingTablesAndColumns(*Tables)
        }
    }
}