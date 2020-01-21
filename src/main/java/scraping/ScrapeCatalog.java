package scraping;

import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import models.SubjectCode;
import models.Term;
import org.slf4j.Logger;
import scraping.models.Course;
import services.ParseCatalog;

/*
    @Todo: Add annotation for parameter. Fix the method to parse
    @Help: Add annotations, comments to code
 */
public class ScrapeCatalog {

  public static List<Course> scrapeFromCatalog(Logger logger, Term term,
                                               SubjectCode subjectCode)
      throws InterruptedException, ExecutionException, IOException {
    return ParseCatalog.parse(logger,
                              QueryCatalog.queryCatalog(term, subjectCode));
  }

  public static Vector<List<Course>>
  scrapeFromCatalog(Logger logger, Term term, List<SubjectCode> subjectCodes,
                    Integer batchSize) {
    Vector<List<Course>> outputs = new Vector<>();
    QueryCatalog.queryCatalog(term, subjectCodes, batchSize)
        .forEach(rawData -> {
          try {
            outputs.add(ParseCatalog.parse(logger, rawData));
          } catch (IOException e) {
            logger.warn(e.getMessage());
          }
        });
    return outputs;
  }

  public static Vector<List<Course>> scrapeAllFromCatalog(Logger logger,
                                                          Term term,
                                                          String forSchool,
                                                          Integer batchSize) {
    return scrapeFromCatalog(logger, term, SubjectCode.allSubjects(forSchool),
                             batchSize);
  }
}
