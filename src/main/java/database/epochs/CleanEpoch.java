package database.epochs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import nyu.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Utils;

public final class CleanEpoch {

  private static Logger logger =
      LoggerFactory.getLogger("database.epochs.CleanEpoch");

  public static void cleanEpochsUpTo(Connection conn, int epoch)
      throws SQLException {
    PreparedStatement stmt =
        conn.prepareStatement("DELETE FROM epochs WHERE epochs.id < ?");
    stmt.setInt(1, epoch);
    stmt.executeUpdate();
  }

  public static void cleanEpochs(Connection conn, Term term)
      throws SQLException {
    PreparedStatement stmt =
        conn.prepareStatement("DELETE FROM epochs WHERE epochs.term_id = ?");
    stmt.setInt(1, term.getId());
    if (stmt.executeUpdate() == 0)
      throw new SQLException("couldn't find term=" + term);
  }

  public static void cleanEpochsUpTo(Connection conn, Term term)
      throws SQLException {
    Integer epoch = LatestCompleteEpoch.getLatestEpoch(conn, term);
    if (epoch == null)
      return;

    PreparedStatement stmt = conn.prepareStatement(
        "DELETE FROM epochs WHERE epochs.term_id = ? AND epochs.id < ?");
    Utils.setArray(stmt, term.getId(), epoch).executeUpdate();
  }

  public static void cleanEpoch(Connection conn, int epoch)
      throws SQLException {
    PreparedStatement stmt =
        conn.prepareStatement("DELETE FROM epochs WHERE epochs.id = ?");
    stmt.setInt(1, epoch);
    if (stmt.executeUpdate() == 0)
      throw new SQLException("couldn't find epoch=" + epoch);
  }
}
