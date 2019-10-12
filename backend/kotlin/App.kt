import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import database.*
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils

class App() : CliktCommand(invokeWithoutSubcommand = true) {
    // private val logLevel by option(help = "Set the logging level.")
    //     .switch(
    //         "--debug" to Logging.DEBUG,
    //         "--info" to Logging.INFO,
    //         "--warn" to Logging.WARN,
    //         "--error" to Logging.ERROR
    //     ).default(Logging.WARN)

    private val logger = KotlinLogging.logger {}

    override fun run() {
        connectToDatabase()
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

fun main(args: Array<String>) {
    App().main(args)
}
