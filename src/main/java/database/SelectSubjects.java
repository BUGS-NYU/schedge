package database;

import static utils.Nyu.*;

import java.sql.*;
import java.util.*;
import utils.Utils;

public class SelectSubjects {
  private static final String SELECT_SCHOOLS = "SELECT id, name, code FROM schools WHERE term = ?";

  private static final String SELECT_SUBJECTS =
      "SELECT school, code, name FROM subjects WHERE term = ?";

  public static ArrayList<School> selectSchoolsForTerm(Connection conn, Term term)
      throws SQLException {
    var subjectsForSchool = selectSubjects(conn, term);

    try (var schoolSel = conn.prepareStatement(SELECT_SCHOOLS)) {
      Utils.setArray(schoolSel, term.json());

      var schools = new ArrayList<School>();

      try (ResultSet rs = schoolSel.executeQuery()) {
        while (rs.next()) {
          var id = rs.getInt("id");
          var name = rs.getString("name");
          var code = rs.getString("code");

          var school = new School(name, code, subjectsForSchool.get(id));
          schools.add(school);
        }
      }

      return schools;
    }
  }

  public static ArrayList<Subject> selectSubjectsForTerm(Connection conn, Term term)
      throws SQLException {
    var subjects = selectSubjects(conn, term);

    var outputList = new ArrayList<Subject>();
    for (var entry : subjects.entrySet()) {
      outputList.addAll(entry.getValue());
    }

    return outputList;
  }

  private static HashMap<Integer, ArrayList<Subject>> selectSubjects(Connection conn, Term term)
      throws SQLException {
    var schools = new HashMap<Integer, ArrayList<Subject>>();

    try (var subjectSel = conn.prepareStatement(SELECT_SUBJECTS)) {
      Utils.setArray(subjectSel, term.json());

      try (ResultSet rs = subjectSel.executeQuery()) {
        while (rs.next()) {
          var school = rs.getInt("school");
          var code = rs.getString("code");
          var name = rs.getString("name");

          var subject = new Subject(code, name);
          schools.computeIfAbsent(school, (k) -> new ArrayList<>()).add(subject);
        }
      }

      return schools;
    }
  }
}
