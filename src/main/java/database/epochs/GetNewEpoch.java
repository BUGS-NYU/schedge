package database.epochs;

import java.sql.*;
import java.time.Instant;
import org.slf4j.*;
import types.Term;
import utils.Utils;

public final class GetNewEpoch {

  private static Logger logger =
      LoggerFactory.getLogger("database.epochs.GetEpoch");

  public static int getNewEpoch(Connection conn, Term term)
      throws SQLException {
    PreparedStatement stmt =
        conn.prepareStatement("INSERT INTO epochs (started_at, term) "
                                  + "VALUES (?, ?)",
                              Statement.RETURN_GENERATED_KEYS);
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
