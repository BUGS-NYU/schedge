package scraping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
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

  /**
   * Given at instructor, will find the coresponding
   * rmp-id for the instructor.
   * @param instructor
   * @return
   */
  public static Future<Instructor> getLinkAsync(Instructor instructor) {
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

  /**
   * Given the rmp-id, we get the rating.
   * Rating can be either a float or N/A, in the case of N/A, we return 0.0
   * @param url
   * @param id
   * @return
   */
  public static Future<Rating> queryRatingAsync(String url, int id) {
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
            return new Rating(id, -1, -1.0f, new ArrayList<>());
          }

          MetaReview meta = parseRating(resp.getResponseBody());
          return new Rating(id, Integer.parseInt(url),
                            meta.getRating(), meta.getReviews());
        });
  }

  public static MetaReview parseRating(String rawData) {
    rawData = rawData.trim();
    List<String> reviews = new ArrayList<>();
    if (rawData == null || rawData.equals("")) {
      logger.warn("Got bad data: empty string");
      return new MetaReview(null, reviews);
    }
    Document doc = Jsoup.parse(rawData);
    Element body = doc.selectFirst("div#root");
    if (body == null)
      return new MetaReview(null, reviews);
    Element ratingBody =
        body.selectFirst("div.TeacherInfo__StyledTeacher-ti1fio-1.fIlNyU");
    Element ratingInnerBody = ratingBody.selectFirst("div").selectFirst(
        "div.RatingValue__AvgRating-qw8sqy-1.gIgExh");
    String ratingValue =
        ratingInnerBody
            .selectFirst("div.RatingValue__Numerator-qw8sqy-2.gxuTRq")
            .html()
            .trim();

    Element reviewBody = body
            .selectFirst("div.react-tabs")
            .selectFirst("div.TeacherRatingTabs__StyledTabPage-pnmswv-2.iaploH")
            .selectFirst("div.react-tabs__tab-panel.react-tabs__tab-panel--selected");
    Elements reviewEls =
        reviewBody.select("ul.RatingsList__RatingsUL-hn9one-1.kHITzZ")
            .select("li");
    for(Element review : reviewEls) {
      Element reviewElement = review.selectFirst("div.Rating__StyledRating-sc-1rhvpxz-0.qSJvr");
      if(reviewElement != null) {
        reviewElement = reviewElement.selectFirst("div.Rating__RatingInfo-sc-1rhvpxz-2.coQIDo")
                .selectFirst("div.Comments__StyledComments-dzzyvm-0.dEfjGB");
        reviews.add(reviewElement.html());
      }
    }

    try {
      return new MetaReview(Float.parseFloat(ratingValue), reviews);
    } catch (NumberFormatException exception) {
      logger.warn("The instructor exist but having N/A rating");
      return new MetaReview(
              null, reviews
      );
    }
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
          element.selectFirst("span.sub").toString();
      if (school.contains("New York University") || school.contains("NYU")) {
        return element.selectFirst("a").attr("href").split("=")[1];
      }
    }

    return null;
  }

  private static class MetaReview {
    private Float rating;
    private List<String> reviews;

    public MetaReview(Float rating, List<String> reviews) {
      this.rating = rating;
      this.reviews = reviews;
    }

    public Float getRating() {
      return this.rating;
    }

    public List<String> getReviews() {
      return reviews;
    }
  }
}
