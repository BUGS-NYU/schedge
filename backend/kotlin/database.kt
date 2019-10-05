import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


fun hi() {
    Database.connect(
        "jdbc:postgresql://localhost:12346/test",
        driver = "org.postgresql.Driver",
        user = "user",
        password = "password"
    )
}

