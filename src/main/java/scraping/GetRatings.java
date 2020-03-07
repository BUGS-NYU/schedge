package scraping;

import java.util.Iterator;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.uri.Uri;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraping.models.Instructor;
import scraping.models.Rating;
import scraping.query.GetClient;
import utils.SimpleBatchedFutureEngine;

public final class GetRatings {

  private static Logger logger = LoggerFactory.getLogger("scraping.GetRatings");
  private static final String RMP_ROOT_URL =
      "https://www.ratemyprofessors.com/ShowRatings.jsp?tid=";
  private static final String RMP_URL =
      "https://www.ratemyprofessors.com/search.jsp?queryBy=teacherName&schoolID=675&query=";

  public static Stream<Rating> getRatings(Iterator<Instructor> names,
                                          Integer batchSizeNullable) {
    int batchSize = batchSizeNullable != null
                        ? batchSizeNullable
                        : 50; // @Performance what should this number be?

    // @TODO Change this to actually be correct in terms of types used
    SimpleBatchedFutureEngine<Instructor, Instructor> instructorResults =
        new SimpleBatchedFutureEngine<>(
            names, batchSize, (instructor, __) -> getLinkAsync(instructor));

    SimpleBatchedFutureEngine<Instructor, Rating> engine =
        new SimpleBatchedFutureEngine<>(
            instructorResults, batchSize, (instructor, __) -> {
              try {
                return queryRatingAsync(instructor.name, instructor.id);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });

    return StreamSupport.stream(engine.spliterator(), false)
        .filter(i -> i != null);
  }

  public static Future<Instructor> getLinkAsync(Instructor instructor) {
    String param = instructor.name.replaceAll("\\s+", "+");
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

  private static Future<Rating> queryRatingAsync(String url, int id) {
    Request request = new RequestBuilder()
                          .setUri(Uri.create(RMP_ROOT_URL + url))
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
          if (url == null) {
             logger.warn("URL is null for id=" + id);
            return new Rating(id, -1, -1.0f);
          }

          return new Rating(id, Integer.parseInt(url),
                            parseRating(resp.getResponseBody()));
        });
  }

  private static Float parseRating(String rawData) {
    rawData = rawData.trim();
    if (rawData == null || rawData.equals("")) {
      logger.warn("Got bad data: empty string");
      return null;
    }
    Document doc = Jsoup.parse(rawData);
    Element body = doc.selectFirst("div#root");
    if (body == null)
      return null;
    Element ratingBody =
        body.selectFirst("div.TeacherInfo__StyledTeacher-ti1fio-1.fIlNyU");
    Element ratingInnerBody = ratingBody.selectFirst("div").selectFirst(
        "div.RatingValue__AvgRating-qw8sqy-1.gIgExh");
    String ratingValue =
        ratingInnerBody
            .selectFirst("div.RatingValue__Numerator-qw8sqy-2.gxuTRq")
            .html();
    return Float.parseFloat(ratingValue);
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
      // logger.warn("No search Result");
      return null;
    }

    Element innerListings = listings.selectFirst("ul.listings");
    Elements professors = innerListings.select("li");
    for (Element element : professors) {
      String school = element.selectFirst("span.sub").toString();
      if (school.contains("New York University") || school.contains("NYU")) {
        return element.selectFirst("a").attr("href").split("=")[1];
      }
    }

    return null;
  }
}
