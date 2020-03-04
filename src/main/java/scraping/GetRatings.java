package scraping;

import java.util.ArrayList;
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
import scraping.models.Rating;
import scraping.query.GetClient;

public final class GetRatings {
  private static final String RMP_ROOT_URL = "https://www.ratemyprofessors.com";
  private static final String RMP_URL =
      "https://www.ratemyprofessors.com/search.jsp?query=";

  // Look at Later
  //  public static Stream<String> getRatings(Stream<String> names, Integer
  //  batchSizeNullable) {
  //    int batchSize = batchSizeNullable != null
  //            ? batchSizeNullable
  //            : 100; // @Performance what should this number be?
  //    return StreamSupport
  //            .stream(new SimpleBatchedFutureEngine<String, String>(
  //                            names, batchSize,
  //                            (name,
  //                             __) -> {
  //                              return queryRatingAsync(name);
  //                            })
  //                            .spliterator(),
  //                    false)
  //            .filter(i -> i != null);
  //  }

  public static Future<String> queryRatingAsync(String name) throws Exception {
    String link = parseLink(getLinkAsync(name).get());
    if (link == null)
      return null;
    return queryRating(parseLink(getLinkAsync(name).get()), str -> str);
  }

  public static Future<String> getLinkAsync(String name) {
    return getLink(name, str -> str);
  }

  public static <T> Future<T> queryRating(String url,
                                          Function<String, T> transform) {
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
            //@Todo: add error for logger here
            return null;
          }
          return transform.apply(resp.getResponseBody());
        });
  }

  public static <T> Future<T> getLink(String name,
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
            //@Todo: add error for logger here
            return null;
          }
          return transform.apply(resp.getResponseBody());
        });
  }

  public static String parseRating(String rawData) {
    rawData = rawData.trim();
    //@Todo: Error handling here
    Document doc = Jsoup.parse(rawData);
    Element body = doc.selectFirst("div#root");
    //    System.out.println(body.selectFirst("div.App__StyledApp-aq7j9t-0
    //    bISQOd"));
    Element innerBody =
        body.selectFirst("div.App__StyledApp-aq7j9t-0.bISQOd")
            .selectFirst("div.App__Body-aq7j9t-1.cQtlyO")
            .selectFirst(
                "div.PageWrapper__StyledPageWrapper-sc-3p8f0h-0.kKcqwZ")
            .selectFirst(
                "div.TeacherRatingsPage__TeacherBlock-a57owa-1.jJhFVh");
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

  public static String parseLink(String rawData) {
    rawData = rawData.trim();
    //@Todo: Error handling here
    Document doc = Jsoup.parse(rawData);
    Element body = doc.selectFirst("body.search_results");
    Element container = body.selectFirst("div#container");
    Element innerBody = container.selectFirst("div#body");
    Element mainContent = innerBody.selectFirst("div#mainContent");
    Element resBox = mainContent.selectFirst("div#searchResultsBox");
    Element listings = resBox.selectFirst("div.listings-wrap");
    Element innerListings = listings.selectFirst("ul.listings");
    Elements professors = innerListings.select("li");
    for (Element element : professors) {
      String school = element.selectFirst("span.sub").toString();
      if (school.contains("New York University") || school.contains("NYU")) {
        return element.selectFirst("a").attr("href");
      }
    }

    return null;
  }
}
