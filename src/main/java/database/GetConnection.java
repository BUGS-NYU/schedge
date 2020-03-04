package database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.jooq.SQLDialect;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class get connection to the SQLite database using JDBC Driver
 */
public class GetConnection {

  public static final SQLDialect DIALECT = SQLDialect.SQLITE;

  private static HikariDataSource dataSource;

  private static @NotNull String getEnvDefault(String name,
                                               @NotNull String default_value) {
    String value = System.getenv(name);
    if (value == null) {
      return default_value;
    } else
      return value;
  }

  public static Connection getConnection() throws SQLException {
    if (dataSource == null) {
      HikariConfig config = new HikariConfig();
      config.setUsername("schedge");
      config.setPassword("");
      config.setJdbcUrl("jdbc:sqlite:" + System.getProperty("user.dir") +
                        "/local/tables.db");
      // config.addDataSourceProperty("cachePrepStmts", "false");
      dataSource = new HikariDataSource(config);
    }

    return dataSource.getConnection();
  }

  public static void close() {
    try {
      if (dataSource != null)
        dataSource.close();
      dataSource = null;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
