package database;

import static utils.TryCatch.*;

import java.sql.*;
import java.util.*;
import utils.Utils;

public final class Migrations {

  private static final String SCHEMA_QUERY =
      "SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'schedge_meta'";
  private static final String VERSION_QUERY =
      "SELECT value FROM schedge_meta WHERE name = 'version'";

  public static void runMigrations(Connection conn) throws SQLException {
    int version = schemaVersion(conn);
    System.err.println("version: " + version);

    String directory = "/migrations";
    List<String> paths = tcPass(() -> Utils.resourcePaths(directory));
    Collections.sort(paths);

    for (String path : paths) {
      path = path.substring(directory.length());
      int fileVersion = Integer.parseInt(path.split("_")[0].substring(2));

      if (fileVersion > version) {
        // run migration
      }
    }
  }

  private static int schemaVersion(Connection conn) throws SQLException {
    try (PreparedStatement schemaStmt = conn.prepareStatement(SCHEMA_QUERY);
         PreparedStatement versionStmt = conn.prepareStatement(VERSION_QUERY)) {
      ResultSet rs = schemaStmt.executeQuery();
      boolean hasNext = rs.next();
      rs.close();

      if (!hasNext)
        return 0;

      rs = versionStmt.executeQuery();
      hasNext = rs.next();

      if (!hasNext) {
        rs.close();
        return 0;
      }

      String version = rs.getString("value");
      rs.close();

      return Integer.parseInt(version);
    } catch (SQLException e) {
      return 0;
    }
  }
}
