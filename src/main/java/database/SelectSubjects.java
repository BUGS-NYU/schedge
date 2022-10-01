package database;

import static types.Nyu.*;

import java.sql.*;
import java.util.*;
import org.slf4j.*;
import utils.Utils;

public class SelectSubjects {
  private static Logger logger =
      LoggerFactory.getLogger("database.SelectSubjects");

  private static final String SELECT_SCHOOLS =
      "SELECT id, name FROM schools WHERE term = ?";

  private static final String SELECT_SUBJECTS =
      "SELECT school, code, name FROM subjects WHERE term = ?";

  public static ArrayList<School> selectSchoolsForTerm(Connection conn,
                                                       Term term)
      throws SQLException {
    try (var schoolSel = conn.prepareStatement(SELECT_SCHOOLS);
         var subjectSel = conn.prepareStatement(SELECT_SUBJECTS)) {
      Utils.setArray(schoolSel, term.json());

      var schools = new HashMap<Integer, School>();
      try (ResultSet rs = schoolSel.executeQuery()) {
        while (rs.next()) {
          var id = rs.getInt("id");
          var name = rs.getString("name");

          School school = new School(name);

          schools.put(id, school);
        }
      }

      Utils.setArray(subjectSel, term.json());

      try (ResultSet rs = subjectSel.executeQuery()) {
        while (rs.next()) {
          var school = rs.getInt("school");
          var code = rs.getString("code");
          var name = rs.getString("name");

          Subject subject = new Subject(code, name);

          schools.get(school).subjects.add(subject);
        }
      }

      return new ArrayList<School>(schools.values());
    }
  }
}
