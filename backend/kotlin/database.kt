import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.HikariConfig
import java.io.IOException


class DatabaseManager {

    companion object {
        val connection = {
            val config = HikariConfig().also {
                it.jdbcUrl = "jdbc:postgresql://localhost:5432"
                it.driverClassName = "org.postgresql.Driver"
                it.username = "aliu"
                it.password = "docker"
            }
            Database.connect(HikariDataSource(config))
        }
    }
}

