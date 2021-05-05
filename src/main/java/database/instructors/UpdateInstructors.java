package database.instructors;

import static utils.TryCatch.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import scraping.GetRatings;
import scraping.models.Instructor;
import scraping.models.Rating;
import utils.Utils;

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
        conn.prepareStatement("UPDATE instructors SET "
                              + "rmp_rating = ?, rmp_tid  = ? WHERE id = ?");
    Callable1<Rating, Boolean> update = (rating)
        -> Utils.setArray(stmt, rating.rating, rating.rmpTeacherId,
                          rating.instructorId)
                   .executeUpdate() == 1;
    GetRatings.getRatings(instructors.iterator(), batchSizeNullable)
        .filter(rating -> rating.rmpTeacherId != -1 && rating.rating != -1.0f)
        .forEach(tcCreateAssert(update));
  }
}
