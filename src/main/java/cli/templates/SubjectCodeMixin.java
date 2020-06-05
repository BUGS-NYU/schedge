package cli.templates;

import nyu.SubjectCode;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.List;

public final class SubjectCodeMixin {
  private SubjectCodeMixin() {}

  @CommandLine.
  Option(names = "--school", description = "school code: UA, UT, UY, etc")
  private String school;
  @CommandLine.
  Option(names = "--subject", description = "subject code: CSCI, MATH, etc")
  private String subject;

  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;

  public List<SubjectCode> getSubjectCodes() {
    if (school == null) {
      if (subject != null) {
        throw new CommandLine.ParameterException(
            spec.commandLine(),
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
