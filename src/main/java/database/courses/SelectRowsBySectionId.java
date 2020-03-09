package database.courses;

import static database.generated.Tables.COURSES;
import static database.generated.Tables.SECTIONS;

import database.models.Row;
import java.util.List;
import java.util.stream.Stream;
import org.jooq.DSLContext;

public class SelectRowsBySectionId {
  public static Stream<Row> selectRowsBySectionId(DSLContext context, int epoch,
                                                  List<Integer> sectionIds) {
    return SelectRows.selectRows(
        context, COURSES.EPOCH.eq(epoch),
        SECTIONS.ID.in(sectionIds).or(SECTIONS.ASSOCIATED_WITH.in(sectionIds)));
  }
}
