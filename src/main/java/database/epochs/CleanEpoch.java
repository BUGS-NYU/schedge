package database.epochs;

import static database.generated.Tables.EPOCHS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import nyu.Term;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CleanEpoch {

  private static Logger logger =
      LoggerFactory.getLogger("database.epochs.CleanEpoch");

  public static void cleanEpochsUpTo(Connection conn, int epoch)
      throws SQLException {
    PreparedStatement stmt =
        conn.prepareStatement("DELETE FROM epochs WHERE epochs.id < ?");
    stmt.setInt(1, epoch);
    if (!stmt.execute())
      throw new RuntimeException("why did this fail?");
  }

  public static void cleanEpochs(Connection conn, Term term)
      throws SQLException {
    PreparedStatement stmt =
        conn.prepareStatement("DELETE FROM epochs WHERE epochs.term_id = ?");
    stmt.setInt(1, term.getId());
    if (!stmt.execute())
      throw new RuntimeException("why did this fail?");
  }

  public static void cleanEpoch(Connection conn, int epoch)
      throws SQLException {
    PreparedStatement stmt =
        conn.prepareStatement("DELETE FROM epochs WHERE epochs.id = ?");
    stmt.setInt(1, epoch);
    if (!stmt.execute())
      throw new RuntimeException("why did this fail?");
  }
}
