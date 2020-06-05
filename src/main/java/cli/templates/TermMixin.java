package cli.templates;

import nyu.Term;
import picocli.CommandLine;

public class TermMixin {

  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;

  @CommandLine.Option(
      names = "--term",
      description =
          " Term is the shortcut for year and semester. To get term value, take year - 1900 then append \n"
          +
          " ja = 2, sp = 4, su = 6 or fa = 8. \n Eg: Fall 2020 = (2020 - 1900) + 4 = 120 + 4 = 1204")
  private Integer termId;
  @CommandLine.
  Option(names = "--semester", description = "semester: ja, sp, su, or fa")
  private String semester;
  @CommandLine.Option(names = "--year", description = "year to scrape from")
  private Integer year;
  @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true,
                      description = "display a help message")
  boolean displayHelp;

  public Term getTermAllowNull() {
    if (termId != null && (semester != null || year != null)) {
      throw new CommandLine.MutuallyExclusiveArgsException(
          spec.commandLine(),
          "--term and --semester/--year are mutually exclusive");
    } else if (termId == null && semester == null && year == null) {
      return null;
    } else if (termId == null) {
      if (semester == null || year == null) {
        throw new CommandLine.ParameterException(
            spec.commandLine(), "Must provide both --semester AND --year");
      }
      return new Term(semester, year);
    } else {
      return Term.fromId(termId);
    }
  }

  public Term getTerm() {
    Term t = getTermAllowNull();
    if (t == null) {
      throw new CommandLine.ParameterException(
          spec.commandLine(),
          "Must provide at least one. Either --term OR --semester AND --year");
    } else {
      return t;
    }
  }
}
