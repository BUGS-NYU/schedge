package cli.validation;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import nyu.SubjectCode;
import nyu.Term;
import utils.JsonMapper;
import utils.Utils;

public abstract class ValidateCatalogArgs {
  Term term;
  Integer batchSize;
  BiConsumer<Term, Object> writer;

  private ValidateCatalogArgs(Term term, Integer batchSize,
                              BiConsumer<Term, Object> writer) {
    this.term = term;
    this.batchSize = batchSize;
    this.writer = writer;
  }

  public interface ScraperForList {
    Object scrape(Term term, List<SubjectCode> items, Integer batchSize);
  }

  public static ValidateCatalogArgs validate(Integer term, String semester,
                                             Integer year, String school,
                                             String subject, Integer batchSize,
                                             String outputFile) {
    return validate(term, semester, year, school, subject, batchSize,
                    (t, o) -> Utils.writeToFileOrStdout(outputFile, o));
  }

  public static ValidateCatalogArgs validate(Integer term, String semester,
                                             Integer year, String school,
                                             String subject, Integer batchSize,
                                             String outputFile,
                                             boolean prettyPrint) {
    return validate(term, semester, year, school, subject, batchSize,
                    (t, o)
                        -> Utils.writeToFileOrStdout(
                            outputFile, JsonMapper.toJson(o, prettyPrint)));
  }

  public static ValidateCatalogArgs validate(Integer termId, String semester,
                                             Integer year, String school,
                                             String subject, Integer batchSize,
                                             BiConsumer<Term, Object> writer) {
    Term term;
    if (termId == null && semester == null && year == null) {
      throw new IllegalArgumentException(
          "Must provide at least one. Either --term OR --semester AND --year");
    }

    if (termId == null) {
      if (semester == null || year == null) {
        throw new IllegalArgumentException(
            "Must provide both --semester AND --year");
      }
      term = new Term(semester, year);
    } else {
      term = Term.fromId(termId);
    }

    if (school == null) {
      if (subject != null) {
        throw new IllegalArgumentException(
            "--subject doesn't make sense if school is null");
      }

      return new ByTerm(term, batchSize, writer);
    } else if (subject == null) {
      return new BySchool(term, school, batchSize, writer);
    } else {
      if (batchSize != null) {
        throw new IllegalArgumentException(
            "Batch size doesn't make sense when only doing on query");
      }

      return new BySubject(term, new SubjectCode(subject, school), writer);
    }
  }

  public void andRun(ScraperForList forList,
                     BiFunction<Term, SubjectCode, Object> forSingleTon) {
    writer.accept(term, this.run(forList, forSingleTon));
  }

  abstract Object run(ScraperForList forList,
                      BiFunction<Term, SubjectCode, Object> forSingleTon);

  public static class ByTerm extends ValidateCatalogArgs {

    private ByTerm(Term term, Integer batchSize,
                   BiConsumer<Term, Object> writer) {
      super(term, batchSize, writer);
    }

    Object run(ScraperForList forList,
               BiFunction<Term, SubjectCode, Object> forSingleton) {
      return forList.scrape(term, SubjectCode.allSubjects(), batchSize);
    }
  }
  public static class BySubject extends ValidateCatalogArgs {
    private SubjectCode c;

    private BySubject(Term term, SubjectCode c,
                      BiConsumer<Term, Object> writer) {
      super(term, null, writer);
      this.c = c;
    }

    Object run(ScraperForList forList,
               BiFunction<Term, SubjectCode, Object> forSingleton) {
      return forSingleton.apply(term, c);
    }
  }

  public static class BySchool extends ValidateCatalogArgs {
    private String school;
    private BySchool(Term term, String school, Integer batchSize,
                     BiConsumer<Term, Object> writer) {
      super(term, batchSize, writer);
      this.school = school;
    }

    Object run(ScraperForList forList,
               BiFunction<Term, SubjectCode, Object> forSingleton) {
      return forList.scrape(term, SubjectCode.allSubjectsForSchool(school),
                            batchSize);
    }
  }
}
