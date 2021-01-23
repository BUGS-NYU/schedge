package database.instructors;

import database.models.Review;
import java.sql.*;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Utils;

public class SelectInstructors {
  private static Logger logger =
      LoggerFactory.getLogger("database.courses.SelectInstructors");

  public static Stream<Review> selectComment(Connection conn,
                                             String instructorName,
                                             int instructorNameWeight)
      throws SQLException {
    String field = "instructors.name_vec @@ q.query";
    String rank =
        instructorNameWeight + " * ts_rank_cd(instructors.name_vec, q.query";
    logger.info("Retrieving ratings for: " + instructorName);
    PreparedStatement stmt =
        conn.prepareStatement("WITH q (query) AS (SELECT plainto_tsquery(?)) "
                              + "SELECT DISTINCT instructors.id FROM q, instructors "
                              + "WHERE (" + field + ")");
    Utils.setArray(stmt, instructorName);
    List<Integer> result = new ArrayList<>();
    ResultSet rs = stmt.executeQuery();
    while (rs.next()) {
      result.add(rs.getInt(1));
    }
    if (result.size() == 0) {
      rs.close();
      return Stream.empty();
    }
    Integer instructorId = result.get(0);
    PreparedStatement ratingsStmt = conn.prepareStatement(
        "SELECT ratings.* FROM ratings WHERE instructor_id = " + instructorId);
    List<Review> reviews = new ArrayList<>();
    ResultSet ratingsRs = ratingsStmt.executeQuery();
    while (ratingsRs.next()) {
      reviews.add(
          new Review(ratingsRs.getFloat("rmp_rating"), ratingsRs.getString("comment")));
    }
    rs.close();
    ratingsRs.close();
    return reviews.stream();
  }
}
