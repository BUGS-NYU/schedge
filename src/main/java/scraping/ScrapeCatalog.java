package scraping;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import scraping.models.SubjectCode;
import scraping.models.Term;
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

  public static Iterator<List<Course>>
  scrapeFromCatalog(Logger logger, Term term, List<SubjectCode> subjectCodes,
                    Integer batchSize) {
      Iterator<String> origin = QueryCatalog.queryCatalog(term, subjectCodes, batchSize);
    return new Iterator<List<Course>>() {

        @Override
        public boolean hasNext() {
            return origin.hasNext();
        }

        @Override
        public List<Course> next() {
            String val = origin.next();
            if (val == null) return null;
            try {
                return ParseCatalog.parse(logger, val);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    };
  }

  public static Iterator<List<Course>> scrapeAllFromCatalog(Logger logger,
                                                          Term term,
                                                          String forSchool,
                                                          Integer batchSize) throws IOException {
    return scrapeFromCatalog(logger, term, SubjectCode.allSubjects(forSchool),
                             batchSize);
  }
}
