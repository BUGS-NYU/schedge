package database;

import database.models.AugmentedMeeting;
import java.sql.*;
import java.util.*;
import java.util.stream.*;
import utils.Utils;

public final class SelectAugmentedMeetings {
  public static ArrayList<AugmentedMeeting>
  selectAugmentedMeetings(Connection conn, int epoch,
                          List<Integer> registrationNumbers)
      throws SQLException {

    PreparedStatement stmt = conn.prepareStatement(
        "SELECT courses.school, courses.subject, courses.dept_course_id,"
        + "sections.registration_number,sections.instruction_mode "
        + "sections.section_code,sections.section_type,sections.section_status,"
        + "sections.name as section_name,sections.location,meetings.begin_date,"
        + "meetings.end_date,meetings.duration "
        + "FROM courses JOIN sections ON courses.id = sections.course_id "
        + "JOIN meetings ON meetings.section_id = sections.id "
        + "WHERE sections.registration_number = ANY (?)");
    Array regNumberArray =
        conn.createArrayOf("INTEGER", registrationNumbers.toArray());
    Utils.setArray(stmt, regNumberArray);

    ResultSet rs = stmt.executeQuery();
    ArrayList<AugmentedMeeting> meetings = new ArrayList<>();
    while (rs.next()) {
      meetings.add(new AugmentedMeeting(rs));
    }
    rs.close();
    return meetings;
  }
}
