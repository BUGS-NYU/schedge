package database;

import database.models.SectionID;
import java.sql.*;
import java.util.*;
import org.slf4j.*;
import scraping.models.*;
import types.Meeting;
import types.Term;
import utils.Utils;

public class SelectSubjects {
  private static Logger logger =
      LoggerFactory.getLogger("database.SelectSubjects");

  private static final String SELECT_SCHOOLS =
      "SELECT code, name FROM schools WHERE term = ?";

  private static final String SELECT_SUBJECTS =
      "SELECT school, code, name FROM subjects WHERE term = ?";

  public static final class Subject {
    public String school;
    public String code;
    public String name;
  }

  public static final class School {
    public String code;
    public String name;
  }

  public static ArrayList<School> selectSchoolsForTerm(Connection conn,
                                                       Term term)
      throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement(SELECT_SCHOOLS)) {
      Utils.setArray(stmt, term.json());

      ResultSet rs = stmt.executeQuery();
      ArrayList<School> rows = new ArrayList<>();

      while (rs.next()) {
        School school = new School();
        school.code = rs.getString("code");
        school.name = rs.getString("name");

        rows.add(school);
      }

      return rows;
    }
  }

  public static ArrayList<Subject> selectSubjectsForTerm(Connection conn,
                                                         Term term)
      throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement(SELECT_SUBJECTS)) {
      Utils.setArray(stmt, term.json());

      ResultSet rs = stmt.executeQuery();
      ArrayList<Subject> rows = new ArrayList<>();

      while (rs.next()) {
        Subject subject = new Subject();
        subject.school = rs.getString("school");
        subject.code = rs.getString("code");
        subject.name = rs.getString("name");

        rows.add(subject);
      }

      return rows;
    }
  }
}
