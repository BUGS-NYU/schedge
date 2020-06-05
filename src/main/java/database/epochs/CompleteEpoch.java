package database.epochs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import nyu.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Utils;

public final class CompleteEpoch {

  private static Logger logger =
      LoggerFactory.getLogger("database.epochs.CompleteEpoch");

  public static void completeEpoch(Connection conn, Term term, int id)
      throws SQLException {
    PreparedStatement stmt =
        conn.prepareStatement("UPDATE epochs SET completed_at = ? "
                              + "WHERE epochs.id = ? AND epochs.term_id = ?");
    Utils.setArray(stmt, Timestamp.from(Instant.now()), id, term.getId());
    if (stmt.executeUpdate() == 0)
      throw new RuntimeException("why did this fail?");
    logger.info("completed epoch {}", id);
  }
}
