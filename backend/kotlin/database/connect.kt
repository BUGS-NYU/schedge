package database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database


fun connectToDatabase() {
    while (true) {
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
            // logger.info { "Received exception ${e.message}" }
            Thread.sleep(2000L)
        }
    }
}
