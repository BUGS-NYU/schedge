package utils;

import static utils.Try.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

public final class Utils {
  private static final BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));

  public static final class Ref<T> {
    public T value;
  }

  public static <T> Ref<T> ref(T t) {
    var ref = new Ref<T>();
    ref.value = t;
    return ref;
  }

  public static void profileWait() {
    System.err.print("Press Enter to start the profile.");

    // The profile doesn't actually start automatically, you have to attach
    // a different program to this. The goal is simply to prevent the program
    // from making progress until the profiler is attached correctly.
    // - Albert Liu, Feb 04, 2022 Fri 00:58 EST
    try (Scanner scanner = new Scanner(inReader)) {
      scanner.nextLine();
    }
  }

  public static String readResource(String path) {
    InputStream resource = Utils.class.getResourceAsStream(path);

    if (resource == null)
      throw new IllegalArgumentException("Resource doesn't exist: " + path);

    try (var scanner = new Scanner(resource, StandardCharsets.UTF_8).useDelimiter("\\A")) {
      return scanner.next();
    }
  }

  // Read entire file and then get it as a list of lines
  public static List<String> asResourceLines(String path) {
    return Arrays.asList(readResource(path).split("\\n"));
  }

  public static List<String> resourcePaths(String path) throws IOException, URISyntaxException {
    var uri = Objects.requireNonNull(Utils.class.getResource(path)).toURI();

    if (uri.getScheme().equals("jar")) {
      try (var fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
        var myPath = fs.getPath(path);

        try (var files = Files.walk(myPath)) {
          return files
              .filter(Files::isRegularFile)
              .map(Path::toString)
              .collect(Collectors.toList());
        }
      }
    } else {
      var myPath = Paths.get(uri);

      try (var files = Files.walk(myPath)) {
        return files.filter(Files::isRegularFile).map(Path::toString).collect(Collectors.toList());
      }
    }
  }

  public static void writeToFileOrStdout(String file, Object value) {
    if (file == null) {
      System.out.println(value);
    } else {
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
        writer.write(value.toString() + "\n");
        writer.flush();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static String readFromFileOrStdin(String file) {
    var reader = file != null ? tcPass(() -> new BufferedReader(new FileReader(file))) : inReader;
    try (var scanner = new Scanner(reader).useDelimiter("\\A")) {
      return scanner.next();
    }
  }

  public static DayOfWeek parseDayOfWeek(String dayOfWeek) {
    return switch (dayOfWeek) {
      case "Mo", "Mon" -> DayOfWeek.MONDAY;
      case "Tu", "Tue" -> DayOfWeek.TUESDAY;
      case "We", "Wed" -> DayOfWeek.WEDNESDAY;
      case "Th", "Thu" -> DayOfWeek.THURSDAY;
      case "Fr", "Fri" -> DayOfWeek.FRIDAY;
      case "Sa", "Sat" -> DayOfWeek.SATURDAY;
      case "Su", "Sun" -> DayOfWeek.SUNDAY;
      default -> DayOfWeek.valueOf(dayOfWeek);
    };
  }

  public static void setObject(PreparedStatement stmt, int index, Object obj) throws SQLException {
    if (obj == null) {
      throw new IllegalArgumentException("object at index " + index + " is null");
    }

    if (obj instanceof NullWrapper nullable) {
      if (nullable.value == null) {
        stmt.setNull(index, nullable.type);
        return;
      }

      obj = nullable.value;
    }

    stmt.setObject(index, obj);
  }

  public static String getEnvDefault(String name, String defaultValue) {
    var value = System.getenv(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  public static int getEnvDefault(String name, int defaultValue) {
    var value = tcIgnore(() -> Integer.parseInt(System.getenv(name)));
    return value.orElse(defaultValue);
  }

  static class NullWrapper {
    final int type;
    final Object value;

    NullWrapper(int type, Object value) {
      this.type = type;
      this.value = value;
    }
  }

  public static NullWrapper nullable(int type, Object value) {
    return new NullWrapper(type, value);
  }

  public static void setArray(PreparedStatement stmt, Object... objs) {
    tcPass(() -> {
      for (int i = 0; i < objs.length; i++) {
        setObject(stmt, i + 1, objs[i]);
      }
    });
  }

  public static String stackTrace(Throwable t) {
    var sw = new StringWriter();
    var pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    return sw.toString();
  }
}
