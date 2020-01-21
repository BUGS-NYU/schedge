package scraping.models;

public enum SectionStatus {
  Open,
  Closed,
  WaitList,
  Cancelled;

  public static SectionStatus parseStatus(String status) {
    if (status.equals("Wait List")) {
      return SectionStatus.WaitList;
    } else {
      return SectionStatus.valueOf(status);
    }
  }

  public boolean isOpen() { return this == Open; }
}
