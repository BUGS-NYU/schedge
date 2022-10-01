package database;

import static utils.TryCatch.*;

import com.zaxxer.hikari.*;
import java.sql.*;

/**
 * This class get connection to the SQLite database using JDBC Driver
 */
public class GetConnection {

  public interface SQLConsumer {
    void accept(Connection c) throws SQLException;
  }

  public interface SQLFunction<T> {
    T apply(Connection conn) throws SQLException;
  }

  // Takes advantage of class loading semantics
  // https://stackoverflow.com/questions/7420504/threading-lazy-initialization-vs-static-lazy-initialization
  private static final class HolderClass {
    private static final HikariDataSource dataSource;

    static {
      HikariConfig config = new HikariConfig();
      config.setUsername(getEnvDefault("DB_USERNAME", "schedge"));
      config.setPassword(getEnvDefault("DB_PASSWORD", ""));
      config.setJdbcUrl(getEnvDefault(
          "JDBC_URL", "jdbc:postgresql://127.0.0.1:5432/schedge"));

      // We retry a few times so that schedge doesn't hard-crash when
      // running in docker-compose
      var source = tcIgnore(() -> new HikariDataSource(config));
      for (int i = 0; source == null && i < 10; i++) {
        tcIgnore(() -> Thread.sleep(3000));

        source = tcIgnore(() -> new HikariDataSource(config));
      }

      if (source == null)
        source = new HikariDataSource(config);

      dataSource = source;
    }
  }

  private static String getEnvDefault(String name, String default_value) {
    String value = System.getenv(name);
    if (value == null) {
      return default_value;
    } else
      return value;
  }

  public static void withConnection(SQLConsumer f) {
    Connection conn = tcPass(() -> getConnection());

    try {
      conn.setAutoCommit(false);

      f.accept(conn);

      conn.commit();
    } catch (SQLException e) {
      tcIgnore(() -> conn.rollback());

      throw new RuntimeException(e);
    } finally {
      tcIgnore(() -> conn.close());
    }
  }

  public static <T> T withConnectionReturning(SQLFunction<T> f) {
    Connection conn = tcPass(() -> getConnection());

    try {
      conn.setAutoCommit(false);

      T value = f.apply(conn);

      conn.commit();

      return value;
    } catch (SQLException e) {
      tcIgnore(() -> conn.rollback());

      throw new RuntimeException(e);
    } finally {
      tcIgnore(() -> conn.close());
    }
  }

  private static Connection getConnection() throws SQLException {
    return HolderClass.dataSource.getConnection();
  }

  public static void close() { tcFatal(() -> HolderClass.dataSource.close()); }
}
