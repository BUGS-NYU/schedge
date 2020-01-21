package scraping.models;

public enum SectionType {
  LEC("Lecture"),
  RCT("Recitation"),
  LAB("Lab"),
  SEM("Seminar"),
  PCT(" Practicum"),
  INT(" Internship"),
  RSC("Research Tandon's Code"),
  FLD("Field Instruction"),
  SIM("Simulation"),
  LLB("Lecture for Lab (Tandon)"),
  DLX("Distance Learning Hybrid"),
  CLI("Clinic"),
  STU("Studio"),
  STI("Independent Instruction"),
  STG("Group Instruction"),
  CLQ("Colloquium"),
  WKS("Workshop"),
  IND("Independent Study"),
  PRO("Project (Tandon)"),
  GUI("Guided Study (Tandon)"),
  NCR("Non-Credit (Tandon)"),
  PRP("Preparatory"),
  MAM("Maintaining Marticulation");

  private String value;

  SectionType(String value) { this.value = value; }
}
