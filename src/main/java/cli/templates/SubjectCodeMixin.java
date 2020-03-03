package cli.templates;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import nyu.SubjectCode;
import nyu.Term;
import picocli.CommandLine;

public final class SubjectCodeMixin {
  private SubjectCodeMixin() {}

  @CommandLine.Option(names = "--term", description = "term to query from")
  private Integer termId;
  @CommandLine.
  Option(names = "--semester", description = "semester: ja, sp, su, or fa")
  private String semester;
  @CommandLine.Option(names = "--year", description = "year to scrape from")
  private Integer year;
  @CommandLine.
  Option(names = "--school", description = "school code: UA, UT, UY, etc")
  private String school;
  @CommandLine.
  Option(names = "--subject", description = "subject code: CSCI, MATH, etc")
  private String subject;

  public Term getTerm() {
    if (termId == null && semester == null && year == null) {
      throw new IllegalArgumentException(
          "Must provide at least one. Either --term OR --semester AND --year");
    } else if (termId == null) {
      if (semester == null || year == null) {
        throw new IllegalArgumentException(
            "Must provide both --semester AND --year");
      }
      return new Term(semester, year);
    } else {
      return Term.fromId(termId);
    }
  }

  public List<SubjectCode> getSubjectCodes() {
    if (school == null) {
      if (subject != null) {
        throw new IllegalArgumentException(
            "--subject doesn't make sense if school is null");
      }
      return SubjectCode.allSubjects();
    } else if (subject == null) {
      return SubjectCode.allSubjectsForSchool(school);
    } else {
      SubjectCode s = new SubjectCode(subject, school);
      s.checkValid();
      return Arrays.asList(s);
    }
  }
}
