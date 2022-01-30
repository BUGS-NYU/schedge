package scraping;

import java.util.List;
import java.util.stream.Stream;
import nyu.SubjectCode;
import nyu.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraping.models.CatalogQueryData;
import scraping.models.Course;
import scraping.parse.ParseCatalog;
import scraping.query.QueryCatalog;

public class ScrapeCatalog {
  private static Logger logger =
      LoggerFactory.getLogger("scraping.ScrapeCatalog");

  /**
   * Scraping the catalogs from Albert Mobile given multiple subjecs
   * @param term The term for which we should be scraping
   * @param subjectCodes The subjects for which we should be scraping
   * @return Sequence of List of Courses
   */
  public static Stream<List<Course>>
  scrapeFromCatalog(Term term, Iterable<SubjectCode> subjectCodes,
                    Integer batchSize) {
    return QueryCatalog.queryCatalog(term, subjectCodes, batchSize)
        .map(rawData -> {
          try {
            return ParseCatalog.parse(rawData.getData(), rawData.getSubject());
          } catch (Exception e) {
            logger.warn("Catalog parsing threw with term={}, subject={}", term,
                        rawData.getSubject(), e);

            return null;
          }
        })
        .filter(i -> i != null);
  }
}
