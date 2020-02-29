package utils;

import java.io.*;
import java.net.URL;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import kotlin.sequences.Sequence;
import scraping.models.SectionAttribute;

public final class Utils {

  private static BufferedReader inReader =
      new BufferedReader(new InputStreamReader(System.in));

  public static List<String> asResourceLines(String path) {
    InputStream resource = Object.class.getResourceAsStream(path);

    if (resource == null)
      throw new IllegalArgumentException("Resource doesn't exist: " + path);

    // Using the "stupid scanner trick"
    // https://community.oracle.com/blogs/pat/2004/10/23/stupid-scanner-tricks
    return Arrays.asList(
        new Scanner(resource, "UTF-8").useDelimiter("\\A").next().split("\\n"));
  }

  public static void writeToFileOrStdout(String file, Object value) {
    if (file == null) {
      System.out.println(value);
    } else {
      try {
        new BufferedWriter(new FileWriter(file)).write(value.toString() + '\n');
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static String readFromFileOrStdin(String file) {
    if (file == null) {
      return new Scanner(inReader).next("\\A");
    } else {
      try {
        return new Scanner(new BufferedReader(new FileReader(file)))
            .next("\\A");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static DayOfWeek parseDayOfWeek(String dayOfWeek) {
    switch (dayOfWeek) {
    case "Mo":
      return DayOfWeek.MONDAY;
    case "Tu":
      return DayOfWeek.TUESDAY;
    case "We":
      return DayOfWeek.WEDNESDAY;
    case "Th":
      return DayOfWeek.THURSDAY;
    case "Fr":
      return DayOfWeek.FRIDAY;
    case "Sa":
      return DayOfWeek.SATURDAY;
    case "Su":
      return DayOfWeek.SUNDAY;
    default:
      return DayOfWeek.valueOf(dayOfWeek);
    }
  }
}
