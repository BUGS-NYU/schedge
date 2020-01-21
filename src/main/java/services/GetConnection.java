package services;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;

/**
 * This class get connection to the Postgresql database using JDBC Driver
 */
public class GetConnection {

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
      config.setUsername(getEnvDefault("DB_USERNAME", "schedge"));
      config.setPassword(getEnvDefault("DB_PASSWORD", "docker"));
      config.setJdbcUrl(getEnvDefault(
          "JDBC_URL", "jdbc:postgresql://localhost:5432/schedge"));
      dataSource = new HikariDataSource(config);
    }

    return dataSource.getConnection();
  }

  public static void close() throws SQLException {
    if (dataSource != null)
      dataSource.close();
    dataSource = null;
  }
}
