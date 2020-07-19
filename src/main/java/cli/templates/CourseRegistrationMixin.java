package cli.templates;

import picocli.CommandLine;
import register.RegistrationCourse;
import static utils.PolyFill.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CourseRegistrationMixin {
  private CourseRegistrationMixin() {}

  @CommandLine.Option(names = "--courses") Map<Integer, String> courses;

  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;

  public Map<Integer, String> getCourses() {
    if (courses == null) {
      throw new CommandLine.ParameterException(spec.commandLine(),
                                               "Must provide key value pairs");
    }
    return courses;
  }

  public List<RegistrationCourse> convertCourses() {
    List<RegistrationCourse> regCourses = new ArrayList<>();
    for (Map.Entry<Integer, String> entry : courses.entrySet()) {
      String value = entry.getValue();
      if (value.contains(",")) {
        int[] values = Arrays.stream(value.split(","))
                           .mapToInt(Integer::parseInt)
                           .toArray();
        List<Integer> sectionsRelated =
            Arrays.stream(values).boxed().collect(Collectors.toList());
        regCourses.add(new RegistrationCourse(entry.getKey(), sectionsRelated));
      } else if (value.trim().equals("")) {
        regCourses.add(
            new RegistrationCourse(entry.getKey(), new ArrayList<>()));
      } else {
        regCourses.add(new RegistrationCourse(entry.getKey(),
                                              listOf(Integer.parseInt(value))));
      }
    }
    return regCourses;
  }
}
