package database.epochs;

import java.sql.*;
import nyu.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Utils;

public final class LatestCompleteEpoch {

  private static Logger logger =
      LoggerFactory.getLogger("database.epochs.LatestCompleteEpoch");

  public static Integer getLatestEpoch(Connection conn, Term term)
      throws SQLException {
    PreparedStatement stmt = conn.prepareStatement(
        "SELECT max(id) from epochs WHERE completed_at IS NOT NULL "
        + "AND term_id = ? ORDER BY completed_at DESC LIMIT 1");

    try (ResultSet rs = Utils.setArray(stmt, term.getId()).executeQuery()) {
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
