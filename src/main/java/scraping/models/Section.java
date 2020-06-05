package scraping.models;

import nyu.SectionStatus;
import nyu.SectionType;
import nyu.SubjectCode;

import java.util.List;

public class Section {
  public final int registrationNumber;
  public final String sectionCode;
  public final SectionType type;
  public final SectionStatus status;
  public final List<Meeting> meetings;
  public final List<Section> recitations;
  public final Integer waitlistTotal;
  public final SubjectCode subjectCode;

  public Section(SubjectCode code, int registrationNumber, String sectionCode,
                 SectionType type, SectionStatus status, List<Meeting> meetings,
                 List<Section> recitations, Integer waitlistTotal) {
    if (type != SectionType.LEC && recitations != null) {
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