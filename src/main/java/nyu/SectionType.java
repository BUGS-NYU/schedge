package nyu;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SectionType {
  LEC, // Lecture
  RCT, // Recitation
  LAB, // Lab
  SEM, // Seminar
  PCT, // Practicum
  INT, // Internship
  RSC, // Research Tandon's Code
  FLD, // Field Instruction
  SIM, // Simulation
  LLB, // Lecture for Lab (Tandon)
  DLX, // Distance Learning Hybrid
  CLI, // Clinic
  STU, // Studio
  STI, // Independent Instruction
  STG, // Group Instruction
  CLQ, // Colloquium
  WKS, // Workshop
  IND, // independent study
  PRO, // Project (Tandon)
  GUI, // Guided Study (Tandon)
  NCR, // Non-Credit (Tandon)
  PRP, // Preparatory
  MAM, // Maintaining Marticulation
  DLG,
  NCH;

  @JsonValue
  public String getName() {
    switch (this) {
    case LEC:
      return "Lecture";
    case RCT:
      return "Recitation";
    case LAB:
      return "Lab";
    case SEM:
      return "Seminar";
    case IND:
      return "Independent Study";
    case SIM:
      return "Simulation";
    case CLI:
      return "Clinic";
    case FLD:
      return "Field Instruction";
    case WKS:
      return "Workshop";
    case STI:
      return "Independent Instruction";
    case STU:
      return "Studio";
    case STG:
      return "Group Instruction";
    case INT:
      return "Internship";
    case RSC:
      return "Research (Tandon)";
    case CLQ:
      return "Colloquium";
    case PRO:
      return "Project (Tandon)";
    case GUI:
      return "Guided Studies (Tandon)";
    case NCR:
      return "Non-Credit (Tandon)";
    case PRP:
      return "Preparatory";
    case MAM:
      return "Maintaining Marticulation";
    case DLX:
      return "Distance Learning Hybrid";
    case PCT:
      return "Practicum";
    case LLB:
      return "Lecture for Lab";
    default:
      throw new RuntimeException("Unknown name: " + this.toString());
    }
  }
}
