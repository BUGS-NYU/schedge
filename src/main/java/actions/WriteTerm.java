package actions;

import database.*;
import java.sql.*;
import scraping.*;

public class WriteTerm {
  public static void writeTerm(Connection conn, TermScrapeResult data) throws SQLException {
    var term = data.term();
    InsertCourses.clearPrevious(conn, term);

    UpdateSchools.updateSchoolsForTerm(conn, term, data.schools());

    for (var coursesBatch : data.courses()) {
      InsertCourses.insertCourses(conn, term, coursesBatch);
    }
  }
}
