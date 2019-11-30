package services;

import database.generated.tables.records.CoursesRecord;
import models.*;
import org.jooq.DSLContext;
import org.jooq.InsertQuery;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import database.generated.Tables;

public class InsertCourses {
  public static void insertCourses(Logger logger, List<Course> courses) {
    try (Connection conn = GetConnection.getConnection()) {
      DSLContext context = DSL.using(conn, SQLDialect.POSTGRES);
      for (Course c : courses) {
        InsertQuery<CoursesRecord> query = context.insertQuery(Tables.COURSES);
      }
    } catch (SQLException e) {
    }
  }

  public static void insertCourse(Logger logger, Course course) {}
}