package register;

import java.util.List;

public class RegistrationCourse {
    int registrationNumber;
    List<Integer> sectionsRelated;
    String waitList;
    int permissionNo;
    Float units;

    public RegistrationCourse(int registrationNumber, List<Integer> sectionsRelated,
                              String waitList, Integer permissionNo, Float units) {
        this.registrationNumber = registrationNumber;
        this.sectionsRelated = sectionsRelated;
        this.waitList = waitList;
        this.permissionNo = permissionNo;
        this.units = units;
    }

}
