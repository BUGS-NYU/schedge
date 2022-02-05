package database.epochs;

import java.sql.*;
import org.slf4j.*;
import types.Term;
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
        conn.prepareStatement("DELETE FROM epochs WHERE epochs.term = ?");
    stmt.setString(1, term.json());
    if (stmt.executeUpdate() == 0)
      throw new SQLException("couldn't find term=" + term);
  }

  public static void cleanEpochsUpTo(Connection conn, Term term)
      throws SQLException {
    Integer epoch = LatestCompleteEpoch.getLatestEpoch(conn, term);
    if (epoch == null)
      return;

    PreparedStatement stmt = conn.prepareStatement(
        "DELETE FROM epochs WHERE epochs.term = ? AND epochs.id < ?");
    Utils.setArray(stmt, term.json(), epoch).executeUpdate();
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
