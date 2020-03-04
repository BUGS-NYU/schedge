package scraping;

import scraping.models.Rating;

import java.util.stream.Stream;

public final class GetRatings {

  private static final String searchFunction =
      "https://www.ratemyprofessors.com/search.jsp?queryBy=teacherName&schoolID=675&query=Victor+Shoup";

  public static Stream<Rating> getRatings(Stream<String> names) { return null; }
}
