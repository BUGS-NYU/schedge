package database;

import api.models.Course;
import database.epochs.LatestCompleteEpoch;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nyu.SubjectCode;
import nyu.Term;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectCourses {

  private static Logger logger =
      LoggerFactory.getLogger("database.SelectCourses");

  public static List<Course> selectCourses(DSLContext context, Term term,
                                           List<SubjectCode> codes) {
    int epoch = LatestCompleteEpoch.getLatestEpoch(context, term);
    if (epoch == -1)
      Collections.emptyList();
    return codes.stream()
        .flatMap(code -> selectCourses(context, epoch, code))
        .collect(Collectors.toList());
  }

  public static Stream<Course> selectCourses(DSLContext context, int epoch,
                                             SubjectCode code) {
    return CourseSectionRowsToCourses.courseSectionRowsToCourses(
        SelectCourseSectionRows.selectCourseSectionRows(context, epoch, code));
  }
}
