package services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/*
    @Todo: Add annotation for parameter. Fix the method to parse
    @Help: Add annotations, comments to code
 */
public class Utils {
  public static void writeToFileOrStdOut(String text, String outputFile)
      throws IOException {
    if (outputFile == null) {
      System.out.println(text);
    } else {
      Files.write(Paths.get(outputFile), text.getBytes());
    }
  }

  public static String readFromFileOrStdin(String inputFile)
      throws IOException {
    if (inputFile == null) {
      return new BufferedReader(new InputStreamReader(System.in))
          .lines()
          .collect(Collectors.joining());
    } else {
      return Files.lines(Paths.get(inputFile)).collect(Collectors.joining());
    }
  }
}
