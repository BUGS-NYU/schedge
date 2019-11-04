package database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.cdimascio.dotenv.dotenv
import mu.KLogger
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.IOException


fun connectToDatabase(logger: KLogger) {
    val exceptions = mutableListOf<Exception>()
    for (i in 0..5) {
        try {
            val dataSource = HikariDataSource(HikariConfig().also {
                val env = dotenv()
                it.jdbcUrl = env["JDBC_URL"]
                it.driverClassName = "org.postgresql.Driver"
                it.username = env["username"]
                it.password = env["password"]
            })
            Database.connect(dataSource)
            return;
        } catch (e: Exception) {
            exceptions.add(e)
            // logger.info { "Received exception ${e::class}" }
            Thread.sleep(5000L)
        }
    }

    logger.error(IOException("Failed to connect to database.")) {
        "Received following errors:\n[${exceptions}]"
    }

    transaction {
        // TODO Handle migrations instead of just adding missing tables and columns
        SchemaUtils.createMissingTablesAndColumns(Migrations)
        SchemaUtils.createMissingTablesAndColumns(*Tables)
    }
}
