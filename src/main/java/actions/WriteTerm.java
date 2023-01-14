package actions;

import database.*;
import java.sql.*;
import scraping.*;

public class WriteTerm {
  public static void writeTerm(Connection conn, TermScrapeResult data) throws SQLException {
    var term = data.term;
    InsertCourses.clearPrevious(conn, term);

    UpdateSchools.updateSchoolsForTerm(conn, term, data.getSchools());

    while (data.hasNext()) {
      var coursesBatch = data.next();
      InsertCourses.insertCourses(conn, term, coursesBatch);
    }
  }
}
