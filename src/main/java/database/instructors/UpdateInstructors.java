package database.instructors;

import static utils.TryCatch.*;

import java.sql.*;
import java.util.ArrayList;
import scraping.GetRatings;
import scraping.models.Instructor;
import utils.*;

public class UpdateInstructors {

  public static ArrayList<Instructor> instructorUpdateList(Connection conn)
      throws SQLException {
    PreparedStatement stmt =
        conn.prepareStatement("SELECT id, name FROM instructors");
    ResultSet rs = stmt.executeQuery();
    ArrayList<Instructor> instructors = new ArrayList<>();
    while (rs.next()) {
      instructors.add(new Instructor(rs.getInt("id"), rs.getString("name")));
    }

    return instructors;
  }

  public static void updateInstructors(Connection conn,
                                       Iterable<Instructor> instructors,
                                       Integer batchSizeNullable)
      throws SQLException {
    PreparedStatement stmt =
        conn.prepareStatement("UPDATE instructors SET rmp_rating = ?, "
                              + "rmp_tid = ? WHERE id = ?");

    TryCatch tc = tcNew(e -> {});

    GetRatings.getRatings(instructors.iterator(), batchSizeNullable)
        .filter(rating -> rating.rmpTeacherId != -1 && rating.rating != -1.0f)
        .forEach(rating -> {
          Utils.setArray(stmt, rating.rating, rating.rmpTeacherId,
                         rating.instructorId);

          tc.pass(() -> {
            if (stmt.executeUpdate() != 1) {
              throw new RuntimeException("failed to update instructors");
            }
          });
        });
  }
}
