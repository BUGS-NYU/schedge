package database.instructors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Stream;
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

  public static void addInstructorsRating(Connection conn,
                                          Iterable<Instructor> instructors,
                                          Integer batchSizeNullable)
      throws SQLException {
    PreparedStatement stmt = conn.prepareStatement(
        "INSERT INTO ratings(instructor_id, rmp_rating, rmp_helpful, comment) VALUES(?,?,?,?)");
    GetRatings.getRatings(instructors.iterator(), batchSizeNullable)
        .filter(rating -> rating.rmpTeacherId != -1 && rating.rating != -1.0f)
        .forEach(rating -> {
          try {
            if (Utils
                    .setArray(stmt, rating.instructorId, rating.rating,
                              rating.helpful, rating.comment)
                    .executeUpdate() != 1) {
              throw new RuntimeException("Fail to insert");
            }
          } catch (SQLException e) {
            throw new RuntimeException(e);
          }
        });
  }
}
