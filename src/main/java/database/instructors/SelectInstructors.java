package database.instructors;

import static database.generated.Tables.*;
import database.models.InstructorRow;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SelectInstructors {
    public static Stream<InstructorRow> selectInstructorRow(DSLContext context,
                                                            String name) {
        Result<org.jooq.Record> records =
                context.select()
                .from(INSTRUCTORS)
                .where(INSTRUCTORS.NAME.eq(name))
                .fetch();
        return StreamSupport.stream(records.spliterator(), false)
                .map(r -> new InstructorRow(r.get(INSTRUCTORS.NAME),
                        r.get(INSTRUCTORS.RMP_RATING), r.get(INSTRUCTORS.RMP_TID),
                        getReview(context, r.get(INSTRUCTORS.ID))));
    }

    public static List<String> getReview(DSLContext context, int instructor_id) {
        Result<org.jooq.Record1<String>> records =
                context.select(REVIEWS.REVIEW)
                        .from(REVIEWS)
                        .where(REVIEWS.INSTRUCTOR_ID.eq(instructor_id))
                        .fetch();
        List<String> reviews = new ArrayList<>();
        records.stream().forEach(
                r -> {reviews.add(r.get(REVIEWS.REVIEW));}
        );
        return reviews;
    }
}
