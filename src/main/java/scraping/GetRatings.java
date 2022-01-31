package scraping;

import static utils.TryCatch.*;

import java.util.Iterator;
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
import utils.Client;
import utils.SimpleBatchedFutureEngine;
import utils.TryCatch;

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

    TryCatch tc = tcNew(logger);

    SimpleBatchedFutureEngine<Instructor, Rating> engine =
        new SimpleBatchedFutureEngine<>(
            instructorResults, batchSize,
            (instructor, __)
                -> tc.pass(()
                               -> GetRatings.queryRatingAsync(instructor.name,
                                                              instructor.id)));

    return StreamSupport.stream(engine.spliterator(), false)
        .filter(i -> i != null);
  }

  /**
   * Given at instructor, will find the coresponding
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
    return Client.send(request, (resp, throwable) -> {
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
  private static Future<Rating> queryRatingAsync(String url, int id) {
    Request request = new RequestBuilder()
                          .setUri(Uri.create(RMP_ROOT_URL + url))
                          .setRequestTimeout(60000)
                          .setMethod("GET")
                          .build();

    return Client.send(request, (resp, throwable) -> {
      if (resp == null) {
        logger.error(throwable.getMessage());
        return null;
      }
      if (url == null) {
        logger.warn("URL is null for id=" + id);
        return new Rating(id, -1, -1.0f);
      }

      return new Rating(id, Integer.parseInt(url),
                        parseRating(resp.getResponseBody(), id));
    });
  }

  private static float parseRating(String rawData, int id) {
    rawData = rawData.trim();
    if (rawData == null || rawData.equals("")) {
      logger.warn("Got bad data: empty string");
      return -1.0f;
    }
    Document doc = Jsoup.parse(rawData);
    Element body = doc.selectFirst("div#root");
    if (body == null)
      return -1.0f;
    Element ratingBody =
        body.selectFirst("div.TeacherInfo__StyledTeacher-ti1fio-1.fIlNyU");
    Element ratingInnerBody = ratingBody.selectFirst("div").selectFirst(
        "div.RatingValue__AvgRating-qw8sqy-1.gIgExh");
    String ratingValue =
        ratingInnerBody
            .selectFirst("div.RatingValue__Numerator-qw8sqy-2.gxuTRq")
            .html()
            .trim();
    try {
      return Float.parseFloat(ratingValue);
    } catch (NumberFormatException exception) {
      logger.warn("The instructor with id=" + id +
                  " exists but has an N/A rating");
      return -1.0f;
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
          element.selectFirst("span.sub").toString(); // <- Bugs at this line
      if (school.contains("New York University") || school.contains("NYU")) {
        return element.selectFirst("a").attr("href").split("=")[1];
      }
    }

    return null;
  }
}
