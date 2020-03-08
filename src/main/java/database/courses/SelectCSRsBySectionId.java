package database.courses;

import static database.generated.Tables.COURSES;
import static database.generated.Tables.SECTIONS;

import database.models.CourseSectionRow;
import java.util.List;
import java.util.stream.Stream;

import org.jooq.DSLContext;

public class SelectCSRsBySectionId {
    public static Stream<CourseSectionRow>
    selectCSRsBySectionId(DSLContext context, int epoch,
                             List<Integer> sectionIds) {
        return
                SelectCourseSectionRows.selectCourseSectionRows(
                        context, COURSES.EPOCH.eq(epoch),
                        SECTIONS.ID.in(sectionIds)
                                .or(SECTIONS.ASSOCIATED_WITH.in(sectionIds)));
    }
}
