package scraping.models;

import java.util.List;
import types.Meeting;
import types.SectionStatus;
import types.SectionType;

public class Section {
  public final int registrationNumber;
  public final String sectionCode;
  public final SectionType type;
  public final SectionStatus status;
  public final List<Meeting> meetings;
  public final List<Section> recitations;
  public final Integer waitlistTotal;
  public final String subjectCode;

  public Section(String code, int registrationNumber, String sectionCode,
                 SectionType type, SectionStatus status, List<Meeting> meetings,
                 List<Section> recitations, Integer waitlistTotal) {
    if (type != SectionType.LEC && !recitations.isEmpty()) {
      throw new IllegalArgumentException(
          "If the section type isn't a lecture, it can't have recitations!");
    }

    this.subjectCode = code;
    this.waitlistTotal = waitlistTotal;
    this.registrationNumber = registrationNumber;
    this.sectionCode = sectionCode;
    this.type = type;
    this.status = status;
    this.meetings = meetings;
    this.recitations = recitations;
  }

  public String toString() {
    return "Section(subjectCode=" + subjectCode +
        ",waitlistTotal=" + waitlistTotal +
        ",registrationNumber=" + registrationNumber +
        ",sectionCode=" + sectionCode + ",type=" + type + ",status=" + status +
        ",meetings=" + meetings + ",recitations=" + recitations + ")";
  }
}