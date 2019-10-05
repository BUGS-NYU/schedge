import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.HikariConfig
import java.io.IOException
import javax.sql.DataSource

object Cities : Table() {
    val id = integer("id").autoIncrement().primaryKey() // Column<Int>
    val name = varchar("name", 50) // Column<String>
}

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
            SchemaUtils.create(Cities)
          }
        }
    }

}

