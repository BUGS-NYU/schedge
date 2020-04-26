package database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

/**
 * This class get connection to the SQLite database using JDBC Driver
 */
public class GetConnection {

  public static final SQLDialect DIALECT = SQLDialect.POSTGRES;

  private static HikariDataSource dataSource;

  private static String getEnvDefault(String name, String default_value) {
    String value = System.getenv(name);
    if (value == null) {
      return default_value;
    } else
      return value;
  }

  public static void withContext(Consumer<DSLContext> f) {
    try (Connection conn = getConnection()) {
      f.accept(DSL.using(conn, DIALECT));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T withContextReturning(Function<DSLContext, T> f) {
    try (Connection conn = getConnection()) {
      return f.apply(DSL.using(conn, DIALECT));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static void initIfNecessary() {
    if (dataSource == null) {
      HikariConfig config = new HikariConfig();
      config.setUsername(getEnvDefault("DB_USERNAME", "schedge"));
      config.setPassword(getEnvDefault("DB_PASSWORD", ""));
      config.setJdbcUrl(getEnvDefault(
          "JDBC_URL", "jdbc:postgresql://localhost:5432/schedge"));
      dataSource = new HikariDataSource(config);
    }
  }

  public static Connection getConnection() throws SQLException {
    initIfNecessary();
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
