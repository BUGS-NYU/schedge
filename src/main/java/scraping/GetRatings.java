package scraping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.uri.Uri;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.postgresql.shaded.com.ongres.scram.common.gssapi.Gs2Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraping.models.Instructor;
import scraping.models.Rating;
import scraping.query.GetClient;
import utils.JsonMapper;
import utils.SimpleBatchedFutureEngine;

public final class GetRatings {

  private static Logger logger = LoggerFactory.getLogger("scraping.GetRatings");
  private static final String RMP_URL =
      "https://www.ratemyprofessors.com/search.jsp?queryBy=teacherName&schoolID=675&query=";
  private static final String RMP_ROOT_URL =
      "https://www.ratemyprofessors.com/ShowRatings.jsp?tid=";
  private static final String RMP_RATING_URL =
      "https://www.ratemyprofessors.com/paginate/professors/ratings?tid=";

  /**
   * Two step process, first given the list of instructors,
   * find the coressponding rmp-id. Then using the new rmp-id,
   * query the rating.
   * E.g:
   * For professor "Victor Shoup":
   * 1. We first find the rmp-id on RMP using getLinkAsync
   * -> rmp-id: 1134872
   * 2. We then use the rmp-id to query the rating itself
   * -> https://www.ratemyprofessors.com/ShowRatings.jsp?tid=1134872;
   * @param names
   * @param batchSizeNullable
   * @return
   */
  public static Stream<Rating> getRatings(Iterator<Instructor> names,
                                          Integer batchSizeNullable) {
    int batchSize = batchSizeNullable != null
                        ? batchSizeNullable
                        : 50; // @Performance what should this number be?

    // @TODO Change this to actually be correct in terms of types used
    SimpleBatchedFutureEngine<Instructor, Instructor> instructorsResults =
        new SimpleBatchedFutureEngine<>(
            names, batchSize, (instructor, __) -> getLinkAsync(instructor));

    List<Instructor> filteredRatings =
        StreamSupport.stream(instructorsResults.spliterator(), false)
            .filter(instructor -> instructor.name != null)
            .collect(Collectors.toList());

    SimpleBatchedFutureEngine<Instructor, List<Rating>> ratingsWithPages =
        new SimpleBatchedFutureEngine<>(
            filteredRatings, batchSize,
            (instructor, __)
                -> getPagesAsync(instructor.id,
                                 Integer.parseInt(instructor.name)));

    List<Rating> ratings =
        StreamSupport.stream(ratingsWithPages.spliterator(), false)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

    SimpleBatchedFutureEngine<Rating, List<Rating>> ratingsEngine =
        new SimpleBatchedFutureEngine<>(
            ratings, batchSize,
            (rating, __)
                ->  getRatingsAsync(rating.instructorId, rating.rmpTeacherId,
                      rating.page));

    return
        StreamSupport.stream(ratingsEngine.spliterator(), false).flatMap(Collection::stream);
  }

  /**
   * Given at instructor, will find the corresponding
   * rmp-id for the instructor.
   * @param instructor
   * @return
   */
  private static Future<Instructor> getLinkAsync(Instructor instructor) {
    String param = parseInstructorName(instructor.name);
    Request request = new RequestBuilder()
                          .setUri(Uri.create(RMP_URL + param))
                          .setRequestTimeout(60000)
                          .setMethod("GET")
                          .build();
    return GetClient.getClient()
        .executeRequest(request)
        .toCompletableFuture()
        .handleAsync((resp, throwable) -> {
          if (resp == null) {
            logger.error(throwable.getMessage());
            return null;
          }
          String link = parseLink(resp.getResponseBody());
          if (link == null)
            logger.warn("Instructor query " + instructor.name +
                        " returned no results.");
          return new Instructor(instructor.id, link);
        });
  }

  private static String parseInstructorName(String name) {
    String[] names = name.split("\\s+");
    if (names.length <= 2) {
      return name.replaceAll("\\s+", "+");
    } else {
      return String.join("+", names[0], names[names.length - 1]);
    }
  }

  private static Future<List<Rating>> getPagesAsync(int instructorId,
                                                    int rmpTeacherId) {
    Request request = queryRatingsAsync(rmpTeacherId, 1);
    return GetClient.getClient()
        .executeRequest(request)
        .toCompletableFuture()
        .handleAsync((resp, throwable) -> {
          if (resp == null) {
            logger.error(throwable.getMessage());
            return null;
          }
          Integer pages = getTotalPages(resp.getResponseBody());
          List<Rating> instructorsWithPages = new ArrayList<>();
          for (int page = 1; page <= pages; page++) {
            instructorsWithPages.add(
                new Rating(instructorId, rmpTeacherId, page));
          }
          return instructorsWithPages;
        });
  }

  private static Future<List<Rating>>
  getRatingsAsync(int instructorId, int rmpTeacherId, int page) {
    Request request = queryRatingsAsync(rmpTeacherId, page);
    return GetClient.getClient()
        .executeRequest(request)
        .toCompletableFuture()
        .handleAsync((resp, throwable) -> {
          if (resp == null) {
            logger.error(throwable.getMessage());
            return null;
          }
          return parseRatings(resp.getResponseBody(), instructorId,
                              rmpTeacherId, page);
        });
  }

  private static Request queryRatingsAsync(int rmpTeacherId, int page) {
    Request request =
        new RequestBuilder()
            .setUri(Uri.create(RMP_RATING_URL + rmpTeacherId + "&page=" + page))
            .setRequestTimeout(60000)
            .setMethod("GET")
            .build();
    return request;
  }

  private static String parseLink(String rawData) {
    logger.debug("parsing raw RMP data to link...");
    rawData = rawData.trim();
    if (rawData == null || rawData.equals("")) {
      logger.warn("Got bad data: empty string");
      return null;
    }

    Document doc = Jsoup.parse(rawData);
    Element body = doc.selectFirst("body.search_results");
    Element container = body.selectFirst("div#container");
    Element innerBody = container.selectFirst("div#body");
    Element mainContent = innerBody.selectFirst("div#mainContent");
    Element resBox = mainContent.selectFirst("div#searchResultsBox");
    Element listings = resBox.selectFirst("div.listings-wrap");

    if (listings == null) {
      return null;
    }

    Element innerListings = listings.selectFirst("ul.listings");
    Elements professors = innerListings.select("li.listing.PROFESSOR");
    for (Element element : professors) {
      String school =
          element.selectFirst("span.sub").toString(); // <- Bugs at this line
      if (school.contains("New York University") || school.contains("NYU")) {
        return element.selectFirst("a").attr("href").split("=")[1];
      }
    }

    return null;
  }

  private static Integer getTotalPages(String jsonString) {
    ObjectMapper mapper = new ObjectMapper();
    Integer pages = 1;
    try {
      JsonNode node = mapper.readTree(jsonString);
      pages += node.get("remaining").asInt();
    } catch (JsonProcessingException e) {
      logger.error(e.getMessage());
    }
    return pages;
  }

  private static List<Rating> parseRatings(String jsonString, int instructorId
                                           , Integer rmpTeacherId, int page) {
    ObjectMapper mapper = new ObjectMapper();
    List<Rating> ratings = new ArrayList<>();
    try {
      JsonNode node = mapper.readTree(jsonString);
      ArrayNode jsonRatings = (ArrayNode)node.get("ratings");
      jsonRatings.forEach(rating -> {
        ratings.add(new Rating(instructorId, rmpTeacherId,
                               rating.get("id").asInt(),
                               rating.get("rOverall").asLong(),
                               rating.get("rComments").asText(), page));
      });
    } catch (JsonProcessingException e) {
      logger.error(e.getMessage());
    }
    return ratings;
  }
}
