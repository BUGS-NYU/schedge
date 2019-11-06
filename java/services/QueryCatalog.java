package services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import models.SubjectCode;
import models.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryCatalog implements Iterator<QueryCatalog.LabelledResult> {
  private Logger logger;
  private AlbertClient client;
  private Iterator<AlbertClient.LabelledFuture> responses;

  public static Iterator<LabelledResult> query(Logger logger, Term term,
                                               SubjectCode subject)
      throws IOException, InterruptedException {
    return new QueryCatalog(logger, term, subject);
  }

  public static Iterator<LabelledResult> query(Logger logger, Term term,
                                               List<SubjectCode> subjects)
      throws IOException, InterruptedException {
    return new QueryCatalog(logger, term, subjects);
  }

  private QueryCatalog(Logger logger, Term term, SubjectCode subject)
      throws IOException, InterruptedException {
    this(logger, term, new ArrayList<>(Collections.singleton(subject)));
  }
  private QueryCatalog(Logger logger, Term term, List<SubjectCode> subjects)
      throws IOException, InterruptedException {
    this.logger = LoggerFactory.getLogger(logger.getName());
    if (subjects.size() > 1) {
      logger.info("querying catalog for term={} with multiple subjects...",
                  term);
    }

    ArrayList<AlbertClient.LabelledFuture> futures = new ArrayList<>();
    client = new AlbertClient(this.logger);

    for (SubjectCode subject : subjects) {
      futures.add(client.requestCatalogData(term, subject));
    }
    this.responses = futures.iterator();
  }

  @Override
  public boolean hasNext() {
    return responses.hasNext();
  }

  @Override
  public LabelledResult next() {
    try {
      LabelledResult result = responses.next().get();
      if (!hasNext())
        this.client.close();
      logger.info("Result received for subject={}.", result.subject);
      return result;
    } catch (Exception e) {
      logger.warn("Response threw with message '{}'.", e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public static class LabelledResult {
    SubjectCode subject;
    String result;

    LabelledResult(SubjectCode subject, String result) {
      this.subject = subject;
      this.result = result;
    }

    public SubjectCode component1() { return subject; }
    public String component2() { return result; }
  }
}
