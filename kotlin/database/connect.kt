package database

import Environment
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
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
                it.jdbcUrl = Environment["JDBC_URL"]
                it.driverClassName = "org.postgresql.Driver"
                it.username = Environment["username"]
                it.password = Environment["password"]
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
