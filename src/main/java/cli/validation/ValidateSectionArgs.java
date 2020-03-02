package cli.validation;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import nyu.SubjectCode;
import nyu.Term;
import utils.JsonMapper;
import utils.Utils;

public abstract class ValidateSectionArgs {
  Term term;
  Integer batchSize;
  Consumer<Object> writer;

  private ValidateSectionArgs(Term term, Integer batchSize,
                              Consumer<Object> writer) {
    this.term = term;
    this.batchSize = batchSize;
    this.writer = writer;
  }

  public static interface ScraperForList {
    Object scrape(Term term, List<SubjectCode> items, Integer batchSize);
  }

  public static ValidateSectionArgs
  validate(Integer term, String semester, Integer year,
           Integer registrationNumber, String school, String subject,
           Integer batchSize, String outputFile) {
    return validate(term, semester, year, registrationNumber, school, subject,
                    batchSize, o -> Utils.writeToFileOrStdout(outputFile, o));
  }

  public static ValidateSectionArgs
  validate(Integer term, String semester, Integer year,
           Integer registrationNumber, String school, String subject,
           Integer batchSize, String outputFile, boolean prettyPrint) {
    return validate(term, semester, year, registrationNumber, school, subject,
                    batchSize,
                    o
                    -> Utils.writeToFileOrStdout(
                        outputFile, JsonMapper.toJson(o, prettyPrint)));
  }

  public static ValidateSectionArgs
  validate(Integer termId, String semester, Integer year,
           Integer registrationNumber, String school, String subject,
           Integer batchSize, Consumer<Object> writer) {
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

    if (subject != null && school != null && registrationNumber != null) {
      throw new IllegalArgumentException(
          "Must provide either --registration-number OR "
          + "--subject AND --term");
    }

    if (subject == null && school == null) {
      if (batchSize != null) {
        throw new IllegalArgumentException(
            "--batch-size doesn't make sense for a single section");
      }

      if (registrationNumber == null) {
        return new ByTerm(term, batchSize, writer);
      }
      return new ByRegistrationNumber(term, registrationNumber, writer);
    } else if (school != null && subject != null) {
      return new BySubject(term, new SubjectCode(subject, school), batchSize,
                           writer);
    } else if (subject == null) {
      return new BySchool(term, school, batchSize, writer);
    } else {
      throw new IllegalArgumentException(
          "--subject doesn't make sense without a school!");
    }
  }

  public void andRun(ScraperForList forList,
                     BiFunction<Term, Integer, Object> forSingleTon) {
    writer.accept(this.run(forList, forSingleTon));
  }

  abstract Object run(ScraperForList forList,
                      BiFunction<Term, Integer, Object> forSingleTon);

  public static class ByTerm extends ValidateSectionArgs {

    private ByTerm(Term term, Integer batchSize, Consumer<Object> writer) {
      super(term, batchSize, writer);
    }

    Object run(ScraperForList forList,
               BiFunction<Term, Integer, Object> forSingleton) {
      return forList.scrape(term, SubjectCode.allSubjects(), batchSize);
    }
  }
  public static class BySubject extends ValidateSectionArgs {
    private SubjectCode c;

    private BySubject(Term term, SubjectCode c, Integer batchSize,
                      Consumer<Object> writer) {
      super(term, batchSize, writer);
      this.c = c;
    }

    Object run(ScraperForList forList,
               BiFunction<Term, Integer, Object> forSingleton) {
      return forList.scrape(term, Collections.singletonList(c), batchSize);
    }
  }

  public static class BySchool extends ValidateSectionArgs {
    private String school;
    private BySchool(Term term, String school, Integer batchSize,
                     Consumer<Object> writer) {
      super(term, batchSize, writer);
      this.school = school;
    }

    Object run(ScraperForList forList,
               BiFunction<Term, Integer, Object> forSingleton) {
      return forList.scrape(term, SubjectCode.allSubjectsForSchool(school),
                            batchSize);
    }
  }

  public static class ByRegistrationNumber extends ValidateSectionArgs {
    private Integer registrationNumber;
    private ByRegistrationNumber(Term term, Integer registrationNumber,
                                 Consumer<Object> writer) {
      super(term, null, writer);
      this.registrationNumber = registrationNumber;
    }

    Object run(ScraperForList forList,
               BiFunction<Term, Integer, Object> forSingleton) {
      return forSingleton.apply(term, registrationNumber);
    }
  }
}
