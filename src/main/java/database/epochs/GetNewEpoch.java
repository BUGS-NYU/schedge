package database.epochs;

import types.Term;
import org.slf4j.*;
import utils.Utils;

import java.sql.*;
import java.time.Instant;

public final class GetNewEpoch {

  private static Logger logger =
      LoggerFactory.getLogger("database.epochs.GetEpoch");

  public static int getNewEpoch(Connection conn, Term term)
      throws SQLException {
    PreparedStatement stmt =
        conn.prepareStatement("INSERT INTO epochs (started_at, term_id) "
                                  + "VALUES (?, ?)",
                              Statement.RETURN_GENERATED_KEYS);
    Utils.setArray(stmt, Timestamp.from(Instant.now()), term.getId());
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
