package database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.HikariConfig
import java.io.IOException
import javax.sql.DataSource

object DatabaseManager {

    val dataSource: DataSource = HikariConfig().let {
        it.jdbcUrl = "jdbc:postgresql://localhost:5432/schedge"
        it.driverClassName = "org.postgresql.Driver"
        it.username = "schedge"
        it.password = "docker"
        HikariDataSource(it)
    }

    fun setupDatabase() {
        Database.connect(DatabaseManager.dataSource)
        SchemaUtils.createMissingTablesAndColumns(Migrations)
        SchemaUtils.createMissingTablesAndColumns(Courses, Times, Sections)
    }

}

