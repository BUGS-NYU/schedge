package actions;

import static database.InsertCourses.*;
import static database.UpdateSchools.*;

import java.sql.*;
import scraping.*;

public class WriteTerm {
  public static void writeTerm(Connection conn, TermScrapeResult data)
      throws SQLException {
    var term = data.term;
    clearPrevious(conn, term);

    updateSchoolsForTerm(conn, term, data.getSchools());

    while (data.hasNext()) {
      var coursesBatch = data.next();
      insertCourses(conn, term, coursesBatch);
    }
  }
}
