package database;

import java.sql.*;
import java.time.Instant;
import org.slf4j.*;
import types.*;
import utils.*;

public class Epoch {

  private static Logger logger = LoggerFactory.getLogger("database.Epoch");

  public static Integer getLatestEpoch(Connection conn, Term term)
      throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement(
             "SELECT max(id) from epochs WHERE completed_at IS NOT NULL "
             + "AND term = ? LIMIT 1")) {

      try (ResultSet rs = Utils.setArray(stmt, term.json()).executeQuery()) {
        if (!rs.next()) {
          logger.info("Couldn't find epoch for term=" + term);
          return null;
        }
        int e = rs.getInt(1);
        if (rs.wasNull()) {
          logger.info("Couldn't find epoch for term=" + term);
          return null;
        } else {
          logger.info("found epoch=" + e + " for term=" + term);
          return e;
        }
      }
    }
  }

  public static int getNewEpoch(Connection conn, Term term)
      throws SQLException {
    try (PreparedStatement stmt =
             conn.prepareStatement("INSERT INTO epochs (started_at, term) "
                                       + "VALUES (?, ?)",
                                   Statement.RETURN_GENERATED_KEYS)) {
      Utils.setArray(stmt, Timestamp.from(Instant.now()), term.json());

      if (stmt.executeUpdate() != 1)
        throw new RuntimeException("why did this fail?");

      try (ResultSet rs = stmt.getGeneratedKeys()) {
        if (!rs.next()) {
          throw new RuntimeException("why did this fail?");
        }
        return rs.getInt(1);
      }
    }
  }

  public static void completeEpoch(Connection conn, Term term, int id)
      throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement(
             "UPDATE epochs SET completed_at = ? "
             + "WHERE epochs.id = ? AND epochs.term = ?")) {
      Utils.setArray(stmt, Timestamp.from(Instant.now()), id, term.json());

      if (stmt.executeUpdate() == 0)
        throw new RuntimeException("why did this fail?");

      logger.info("completed epoch {}", id);
    }
  }
}
