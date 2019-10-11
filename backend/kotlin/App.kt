import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import database.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import javax.sql.DataSource

class App() : CliktCommand(invokeWithoutSubcommand = true) {
    private val logLevel by option(help = "Set the logging level.")
        .switch(
            "--debug" to Logging.DEBUG,
            "--info" to Logging.INFO,
            "--warn" to Logging.WARN,
            "--error" to Logging.ERROR
        ).default(Logging.WARN)

    private val dataSource: DataSource = HikariConfig().let {
        it.jdbcUrl = "jdbc:postgresql://db:5432/schedge"
        it.driverClassName = "org.postgresql.Driver"
        it.username = "schedge"
        it.password = "docker"
        HikariDataSource(it)
    }

    private val logger: Logging
        get() {
            return Logging.getLogger(logLevel)
        }

    override fun run() {
        Database.connect(dataSource)
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
    for (str in args) {
        println(str)
    }
    App().main(args)
}
