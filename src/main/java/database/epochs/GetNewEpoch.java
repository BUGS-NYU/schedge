package database.epochs;

import java.sql.*;
import java.time.Instant;
import nyu.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Utils;

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
      return rs.getInt(1);
    }
  }
}
