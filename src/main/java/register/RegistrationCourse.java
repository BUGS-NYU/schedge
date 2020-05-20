package register;

import java.util.List;

public class RegistrationCourse {
  int registrationNumber;
  List<Integer> sectionsRelated;
  String waitList;
  int permissionNo;
  Float units;

  public RegistrationCourse(int registrationNumber,
                            List<Integer> sectionsRelated) {
    this.registrationNumber = registrationNumber;
    this.sectionsRelated = sectionsRelated;
    //        this.waitList = waitList;
    //        this.permissionNo = permissionNo;
    //        this.units = units;
  }

  public String toString() {
    return "Registration number: " + registrationNumber +
        " section-related: " + sectionsRelated.toString();
  }
}
