package cli.templates;

import nyu.Term;
import picocli.CommandLine;

public class TermMixin {
    @CommandLine.Option(names = "--term", description = "term to query from")
    private Integer termId;
    @CommandLine.
            Option(names = "--semester", description = "semester: ja, sp, su, or fa")
    private String semester;
    @CommandLine.Option(names = "--year", description = "year to scrape from")
    private Integer year;

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
}
