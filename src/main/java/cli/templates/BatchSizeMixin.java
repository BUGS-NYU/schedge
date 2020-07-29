package cli.templates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import picocli.CommandLine;
import register.RegistrationCourse;

public class BatchSizeMixin {
  private BatchSizeMixin() {}

  @CommandLine.Option(names = "--batch-size-catalog",
                      description = "batch size for querying the catalog")
  private Integer catalog;
  @CommandLine.Option(names = "--batch-size-sections",
                      description = "batch size for querying sections")
  private Integer sections;

  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;

  public int getCatalog(int defaultValue) {
    if (catalog == null)
      return defaultValue;
    else
      return catalog;
  }

  public int getSections(int defaultValue) {
    if (sections == null)
      return defaultValue;
    else
      return sections;
  }
}
