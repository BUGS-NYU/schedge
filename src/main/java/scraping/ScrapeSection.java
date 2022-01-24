package scraping;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import models.Section;
import nyu.*;
import org.slf4j.*;
import scraping.parse.*;
import scraping.query.*;

public class ScrapeSection {
  private static Logger logger =
      LoggerFactory.getLogger("scraping.ScrapeCatalog");

  /**
   * Scraping the catalogs from Albert Mobile given multiple subjecs
   * @param term The term for which we should be scraping
   * @param subjectCodes The subjects for which we should be scraping
   * @return Sequence of List of Courses
   */
  public static Stream<Section>
  scrapeFromSection(Term term, List<SubjectCode> subjectCodes,
                    Integer batchSize, Integer batchSizeSections) {
    return QuerySection
        .querySections(
            term,
            ParseCatalog.parseRegistrationNumber(
                QueryCatalog.queryCatalog(term, subjectCodes, batchSize)
                    .collect(Collectors.toList())
                    .toString()),
            batchSizeSections)
        .map(data -> ParseSection.parse(data));
  }

  public static Section scrapeFromSection(Term term, int registrationNumber) {
    return ParseSection.parse(
        QuerySection.querySection(term, registrationNumber));
  }
}
