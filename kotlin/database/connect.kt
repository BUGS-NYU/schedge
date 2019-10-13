package database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import java.io.IOException
import mu.KLogger


fun connectToDatabase(logger: KLogger) {
    val exceptions = mutableListOf<Exception>()
    for (i in 0..5) {
        try {
            val dataSource = HikariDataSource(HikariConfig().also {
                it.jdbcUrl = "jdbc:postgresql://db:5432/schedge"
                it.driverClassName = "org.postgresql.Driver"
                it.username = "schedge"
                it.password = "docker"
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
}
