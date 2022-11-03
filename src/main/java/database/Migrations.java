package database;

import static utils.Try.*;

import java.sql.*;
import java.util.*;
import org.slf4j.*;
import utils.Utils;

public final class Migrations {
  private static Logger logger = LoggerFactory.getLogger("database.Migrations");

  private static final String SCHEMA_QUERY =
      "SELECT 1 FROM information_schema.tables "
      + "WHERE table_schema = 'public' AND table_name = 'schedge_meta'";

  private static final String VERSION_QUERY =
      "SELECT value FROM schedge_meta WHERE name = 'version'";

  private static final String VERSION_UPDATE =
      "UPDATE schedge_meta SET updated_at = NOW(), value = ? "
      + "WHERE name = 'version'";

  public static void runMigrations(Connection conn) throws SQLException {
    int version = schemaVersion(conn);

    String directory = "/migrations";
    List<String> paths = tcPass(() -> Utils.resourcePaths(directory));
    Collections.sort(paths);

    boolean ranMigration = false;
    try (Statement stmt = conn.createStatement();
         PreparedStatement updateVersionStmt =
             conn.prepareStatement(VERSION_UPDATE)) {

      for (String path : paths) {
        // /migrations/V01_blah_blah.sql
        String name = path.substring(directory.length() + 2);
        int fileVersion = Integer.parseInt(name.split("_")[0]);

        if (fileVersion > version) {
          for (String sql : parseMigration(path)) {
            stmt.execute(sql);
          }
          Utils.setArray(updateVersionStmt, Integer.toString(fileVersion));
          if (updateVersionStmt.executeUpdate() == 0) {
            throw new RuntimeException("Failed to finish schema update for " +
                                       path);
          }

          conn.commit();

          logger.info("Finished migration for V{}", name);
          ranMigration = true;
        }
      }
    }

    if (!ranMigration) {
      logger.info("No migrations to run, database is up-to-date");
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

  private static ArrayList<String> parseMigration(String path)
      throws SQLException {
    ArrayList<String> statements = new ArrayList<String>();
    StringBuilder builder = new StringBuilder();

    for (String line : Utils.asResourceLines(path)) {
      line = line.trim();
      if (line.startsWith("--"))
        continue;

      boolean shouldAdd = true;
      int begin = 0;
      for (int i = 0; i < line.length(); i++) {
        char c = line.charAt(i);
        switch (c) {
        case '\'':
        case '"': {
          for (int j = i + 1; j < line.length(); j++) {
            if (line.charAt(j) == c) {
              i = j;
              break;
            }
          }

          break;
        }

        case '-': {
          if (i > 0 && line.charAt(i - 1) == '-') {
            builder.append(line.substring(begin, i - 1));
            shouldAdd = false;
            i = line.length();
          }
          break;
        }

        case ';': {
          builder.append(line.substring(begin, i));
          statements.add(builder.toString());
          builder.setLength(0);
          begin = i + 1;
          break;
        }

        default:
          break;
        }
      }

      if (shouldAdd) {
        builder.append(line.substring(begin));
        builder.append(' ');
      }
    }

    String s = builder.toString().trim();
    if (s.length() > 0)
      statements.add(s);

    return statements;
  }
}
