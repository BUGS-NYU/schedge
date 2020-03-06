package scraping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import scraping.models.Rating;
import scraping.query.GetClient;

public final class GetRatings {

  private static Logger logger =
      LoggerFactory.getLogger("scraping.rating.GetRatings");
  private static final String RMP_ROOT_URL =
      "https://www.ratemyprofessors.com/ShowRatings.jsp?tid=";
  private static final String RMP_URL =
      "https://www.ratemyprofessors.com/search.jsp?queryBy=teacherName&schoolID=675&query=";

  private static class FirstScrapeResult {
    long link;
    float rating;

    public FirstScrapeResult(long link, float rating) {
      this.link = link;
      this.rating = rating;
    }
  }

  public static Stream<Rating> getRatings(Iterator<String> names,
                                          Integer batchSizeNullable) {
    return getScrapeResult(names, batchSizeNullable)
        .map(firstScrapeResult
             -> new Rating(firstScrapeResult.link, firstScrapeResult.rating));
  }

  private static Stream<FirstScrapeResult>
  getScrapeResult(Iterator<String> names, Integer batchSizeNullable) {
    int batchSize = batchSizeNullable != null
                        ? batchSizeNullable
                        : 100; // @Performance what should this number be?
    return StreamSupport
        .stream(new SimpleBatchedFutureEngine<>(
                    names, batchSize,
                    (name, __) -> {
                      try {
                        String link = parseLink(getLinkAsync(name).get());
                        return queryRatingAsync(link);
                      } catch (Exception e) {
                        throw new RuntimeException(e);
                      }
                    })
                    .spliterator(),
                false)
        .filter(i -> i != null);
  }

  public static Future<String> getLinkAsync(String name) {
    return getLink(name, str -> str);
  }

  private static Future<FirstScrapeResult> queryRatingAsync(String url) {
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
            return new FirstScrapeResult(0, 0);
          }
          return new FirstScrapeResult(
              Long.parseLong(url),
              Float.parseFloat(parseRating(resp.getResponseBody())));
        });
  }

  private static <T> Future<T> getLink(String name,
                                       Function<String, T> transform) {
    String param = name.replaceAll("\\s+", "+");
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
          return transform.apply(resp.getResponseBody());
        });
  }

  private static String parseRating(String rawData) {
    rawData = rawData.trim();
    if (rawData == null || rawData.equals("")) {
      logger.warn("Got bad data: empty string");
      return null;
    }
    Document doc = Jsoup.parse(rawData);
    Element body = doc.selectFirst("div#root");
    Element ratingBody =
        body.selectFirst("div.TeacherInfo__StyledTeacher-ti1fio-1.fIlNyU");
    Element ratingInnerBody = ratingBody.selectFirst("div").selectFirst(
        "div.RatingValue__AvgRating-qw8sqy-1.gIgExh");
    String ratingValue =
        ratingInnerBody
            .selectFirst("div.RatingValue__Numerator-qw8sqy-2.gxuTRq")
            .html();
    return ratingValue;
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
      logger.warn("No search Result");
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
