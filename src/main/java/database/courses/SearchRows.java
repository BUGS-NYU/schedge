package database.courses;

import static database.generated.Tables.*;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.groupConcat;

import database.models.FullRow;
import database.models.Row;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import nyu.Meeting;
import nyu.SubjectCode;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchRows {

  private static Logger logger =
      LoggerFactory.getLogger("database.courses.SearchCourses");
}
