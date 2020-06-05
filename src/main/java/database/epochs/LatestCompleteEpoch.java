package database.epochs;

import nyu.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class LatestCompleteEpoch {

  private static Logger logger =
      LoggerFactory.getLogger("database.epochs.LatestCompleteEpoch");

  public static Integer getLatestEpoch(Connection conn, Term term)
      throws SQLException {
    PreparedStatement stmt = conn.prepareStatement(
        "SELECT max(id) from epochs WHERE completed_at IS NOT NULL "
        + "AND term_id = ? LIMIT 1");

    try (ResultSet rs = Utils.setArray(stmt, term.getId()).executeQuery()) {
      if (!rs.next()) {
        logger.info("Couldn't find epoch for term=" + term);
        return null;
      }
      int e = rs.getInt(1);
      logger.info("found epoch=" + e + " for term=" + term);
      return e;
    }
  }
}
