package database.instructors;

import java.sql.*;
import nyu.SubjectCode;
import utils.Utils;

public final class UpsertInstructor {

  public static void upsertInstructor(Connection conn, SubjectCode subject,
                                      int sectionId, String instructor)
      throws SQLException {

    PreparedStatement stmt = conn.prepareStatement(
        "SELECT id from instructors WHERE subject = ? AND school = ? AND name = ?");
    Utils.setArray(stmt, subject.code, subject.school, instructor);
    ResultSet rs = stmt.executeQuery();
    int instructorId;
    if (!rs.next()) {
      rs.close();
      PreparedStatement createInstructor = conn.prepareStatement(
          "INSERT INTO instructors (name, subject, school) VALUES (?, ?, ?)",
          Statement.RETURN_GENERATED_KEYS);

      Utils.setArray(createInstructor, instructor, subject.code,
                     subject.school);
      if (stmt.executeUpdate() == 0)
        throw new RuntimeException("Why bro");
      rs = stmt.getGeneratedKeys();
      if (!rs.next())
        throw new RuntimeException("man c'mon");
      instructorId = rs.getInt("id");
      rs.close();
    } else {
      instructorId = rs.getInt("id");
      rs.close();
    }

    PreparedStatement addTeachingRelation = conn.prepareStatement(
        "INSERT INTO is_teaching_section (instructor_id, section_id, instructor_name) "
        + "VALUES (?, ?, ?)");

    Utils.setArray(addTeachingRelation, instructorId, sectionId, instructor);

    if (addTeachingRelation.executeUpdate() != 1)
      throw new RuntimeException("wtf dude");
  }
}
