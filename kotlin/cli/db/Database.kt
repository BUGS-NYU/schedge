package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import database.Tables
import database.connectToDatabase
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class Database : CliktCommand(name = "db") {

    init {
        this.subcommands(DbAdd(), Serve())
    }

    override fun run() {
        connectToDatabase(KotlinLogging.logger("db"))
        transaction {
            SchemaUtils.createMissingTablesAndColumns(*Tables)
        }
    }
}