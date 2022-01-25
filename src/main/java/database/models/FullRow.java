package database.models;

import java.sql.*;
import java.util.List;
import nyu.*;

public class FullRow extends Row {
  public String campus;
  public String description;
  public String grading;
  public String notes;
  public String prerequisites;

  public FullRow(ResultSet rs, List<Meeting> meetings) throws SQLException {
    super(rs, meetings);

    this.campus = rs.getString("campus");
    this.description = rs.getString("description");
    this.grading = rs.getString("grading");
    this.notes = rs.getString("notes");
    this.prerequisites = rs.getString("prerequisites");
  }
}
