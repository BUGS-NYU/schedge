package scraping;

import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import scraping.models.SubjectCode;
import scraping.models.Term;
import org.slf4j.Logger;
import scraping.models.SectionAttribute;
import services.ParseCatalog;
import services.ParseSection;

// @Todo: Add more comments
public class ScrapeSection {
  /**
   * Scraping the catalogs from Albert Mobile given one registration number
   *
   * @param term               The term for which we should be scraping
   * @param registrationNumber The registration number for which we should be
   *     scraping
   * @return SectionAttribut Object
   */
  public static SectionAttribute scrapeFromSection(Term term,
                                                   Integer registrationNumber)
      throws ExecutionException, InterruptedException, IOException {
    return ParseSection.parse(
        QuerySection.querySection(term, registrationNumber));
  }

  /**
   * Scraping the catalogs' sections from Albert Mobile given one subject code
   *
   * @param logger      The logger to log to during execution of this service
   * @param term        The term for which we should be scraping
   * @param subjectCode The subject for which we should be scraping
   * @return List of courses
   */
  public static Vector<SectionAttribute>
  scrapeFromCatalogSection(Logger logger, Term term, SubjectCode subjectCode,
                           Integer batchSize)
      throws ExecutionException, InterruptedException, IOException {
    Vector<SectionAttribute> outputs = new Vector<>();
    QuerySection
        .querySection(term,
                      ParseCatalog.parseRegistrationNumber(
                          logger, QueryCatalog.queryCatalog(term, subjectCode)),
                      batchSize)
        .forEach(rawData -> {
          try {
            outputs.add(ParseSection.parse(rawData));
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
    return outputs;
  }

  /**
   * Scraping the catalogs' sections from Albert Mobile given school name
   *
   * @param logger    The logger to log to during execution of this service
   * @param term      The term for which we should be scraping
   * @param forSchool The school we are scraping
   * @return List of courses
   */
  public static Vector<SectionAttribute>
  scrapeFromCatalogSection(Logger logger, Term term, String forSchool,
                           Integer batchSize) throws IOException {
    Vector<SectionAttribute> outputs = new Vector<>();
    QuerySection
        .querySection(
            term,
            ParseCatalog.parseRegistrationNumber(
                logger, QueryCatalog
                            .queryCatalog(
                                term, SubjectCode.allSubjects(forSchool), null)
                            .toString()),
            batchSize)
        .forEach(rawData -> {
          try {
            outputs.add(ParseSection.parse(rawData));
          } catch (IOException e) {
            logger.warn(e.getMessage());
          }
        });
    return outputs;
  }

  /**
   * Scraping the catalogs' sections from Albert Mobile given multiple subject
   * codes
   *
   * @param logger       The logger to log to during execution of this service
   * @param term         The term for which we should be scraping
   * @param subjectCodes The subject for which we should be scraping
   * @return List of courses
   */
  public static Vector<SectionAttribute>
  scrapeFromAllCatalog(Logger logger, Term term, List<SubjectCode> subjectCodes,
                       Integer batchSize) throws IOException {
    Vector<SectionAttribute> outputs = new Vector<>();
    QuerySection
        .querySection(
            term,
            ParseCatalog.parseRegistrationNumber(
                logger,
                QueryCatalog.queryCatalog(term, subjectCodes, null).toString()),
            batchSize)
        .forEach(rawData -> {
          try {
            outputs.add(ParseSection.parse(rawData));
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
    return outputs;
  }
}
