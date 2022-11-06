package utils;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

public final class Utils {
  private static BufferedReader inReader =
      new BufferedReader(new InputStreamReader(System.in));

  public static void profileWait() {
    System.err.print("Press Enter to start the profile.");

    // The profile doesn't actually start automatically, you have to attach
    // a different program to this. The goal is simply to prevent the program
    // from making progress until the profiler is attached correctly.
    //                              - Albert Liu, Feb 04, 2022 Fri 00:58 EST
    Scanner scanner = new Scanner(inReader);
    scanner.nextLine();
  }

  public static List<String> asResourceLines(String path) {
    InputStream resource = Utils.class.getResourceAsStream(path);

    if (resource == null)
      throw new IllegalArgumentException("Resource doesn't exist: " + path);

    // Read entire file and then get it as a list of lines
    return Arrays.asList(
        new Scanner(resource, "UTF-8").useDelimiter("\\A").next().split("\\n"));
  }

  public static List<String> resourcePaths(String path)
      throws IOException, URISyntaxException {
    URI uri = Utils.class.getResource(path).toURI();

    try (FileSystem fileSystem = FileSystems.newFileSystem(
             uri, Collections.<String, Object>emptyMap())) {
      Path myPath = fileSystem.getPath(path);

      return Files.walk(myPath)
          .filter(Files::isRegularFile)
          .map(p -> p.toString())
          .collect(Collectors.toList());
    }
  }

  public static void writeToFileOrStdout(String file, Object value) {
    if (file == null) {
      System.out.println(value);
    } else {
      try {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(value.toString() + "\n");
        writer.flush();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static String readFromFileOrStdin(String file) {
    if (file != null) {
      try {
        return new Scanner(new FileReader(file)).useDelimiter("\\A").next();
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
    return new Scanner(inReader).useDelimiter("\\A").next();
  }

  public static DayOfWeek parseDayOfWeek(String dayOfWeek) {
    switch (dayOfWeek) {
    case "Mo":
    case "Mon":
      return DayOfWeek.MONDAY;
    case "Tu":
    case "Tue":
      return DayOfWeek.TUESDAY;
    case "We":
    case "Wed":
      return DayOfWeek.WEDNESDAY;
    case "Th":
    case "Thu":
      return DayOfWeek.THURSDAY;
    case "Fr":
    case "Fri":
      return DayOfWeek.FRIDAY;
    case "Sa":
    case "Sat":
      return DayOfWeek.SATURDAY;
    case "Su":
    case "Sun":
      return DayOfWeek.SUNDAY;
    default:
      return DayOfWeek.valueOf(dayOfWeek);
    }
  }

  public static boolean deleteFile(File f) {
    if (f.isDirectory()) {
      for (File c : f.listFiles())
        deleteFile(c);
    }
    return f.delete();
  }

  public static void setObject(PreparedStatement stmt, int index, Object obj)
      throws SQLException {
    if (obj == null) {
      throw new IllegalArgumentException("object is null");
    }

    if (obj instanceof NullWrapper) {
      NullWrapper nullable = (NullWrapper)obj;
      if (nullable.value == null) {
        stmt.setNull(index, nullable.type);
        return;
      }

      obj = nullable.value;
    }

    stmt.setObject(index, obj);
  }

  static class NullWrapper {
    int type;
    Object value;

    NullWrapper(int type, Object value) {
      this.type = type;
      this.value = value;
    }
  }

  public static NullWrapper nullable(int type, Object value) {
    return new NullWrapper(type, value);
  }

  public static PreparedStatement setArray(PreparedStatement stmt,
                                           Object... objs) {
    int i = 0;
    try {
      for (i = 0; i < objs.length; i++) {
        setObject(stmt, i + 1, objs[i]);
      }
      return stmt;
    } catch (Exception e) {
      // System.err.println("at index " + i);
      throw new RuntimeException(e);
    }
  }
}
