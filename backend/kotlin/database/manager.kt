package database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.HikariConfig
import java.io.IOException
import javax.sql.DataSource

class DatabaseManager {

    companion object {
        val dataSource : DataSource = {
            val config = HikariConfig().also {
                it.jdbcUrl = "jdbc:postgresql://localhost:5432/"
                it.driverClassName = "org.postgresql.Driver"
                it.username = "schedge"
                it.password = "docker"
            }
            HikariDataSource(config)
        }()
        fun setupDatabase() {
          Database.connect(DatabaseManager.dataSource)
          transaction {
            SchemaUtils.create(Courses)
          }
        }
    }

}

