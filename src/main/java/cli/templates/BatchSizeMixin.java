package cli.templates;

import picocli.CommandLine;

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
