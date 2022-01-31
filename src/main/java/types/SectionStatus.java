package types;

/**
 * Enum class for status of the section based on the availability
 */
public enum SectionStatus {
  Open,      // Open
  Closed,    // Closed
  WaitList,  // Waitlist
  Cancelled; // Cancelled

  public static SectionStatus parseStatus(String status) {
    if (status.equals("Wait List"))
      return WaitList;
    else
      return valueOf(status);
  }

  public boolean isOpen() { return this == Open; }
}
