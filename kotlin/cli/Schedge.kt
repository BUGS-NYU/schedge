package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

internal class Schedge() : CliktCommand(name = "schedge") {

    init {
        this.subcommands(Query(), Parse(), Database())
    }

    override fun run() {
        // connectToDatabase(logger)
        // transaction {
        //     // TODO Handle migrations instead of just adding missing tables and columns
        //     SchemaUtils.createMissingTablesAndColumns(Migrations)
        //     SchemaUtils.createMissingTablesAndColumns(*Tables)
        // }

        // println(models.Subjects)
        // println(models.Schools)
        // logger.error("Nothing's been implemented!")
    }
}


