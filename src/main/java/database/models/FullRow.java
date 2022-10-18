package database.models;

import java.sql.*;
import java.util.List;
import utils.Nyu;

public class FullRow extends Row {
  public String campus;
  public String grading;
  public String notes;

  public FullRow(ResultSet rs, List<Nyu.Meeting> meetings) throws SQLException {
    super(rs, meetings);

    this.campus = rs.getString("campus");
    this.grading = rs.getString("grading");
    this.notes = rs.getString("notes");
  }
}
