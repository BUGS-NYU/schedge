package database.instructors;

import static database.generated.Tables.INSTRUCTORS;
import static database.generated.Tables.REVIEWS;

import java.util.List;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import scraping.GetRatings;
import scraping.models.Instructor;

public class UpdateInstructors {

  public static List<Instructor> instructorUpdateList(DSLContext context) {
    return context.select(INSTRUCTORS.ID, INSTRUCTORS.NAME)
        .from(INSTRUCTORS)
        .fetch()
        .stream()
        .map(row -> new Instructor(row.component1(), row.component2()))
        .collect(Collectors.toList());
  }

  public static void updateInstructors(DSLContext context,
                                       Iterable<Instructor> instructors,
                                       Integer batchSizeNullable) {

    GetRatings.getRatings(instructors.iterator(), batchSizeNullable)
        .filter(rating -> rating.rmpTeacherId != -1)
        .forEach(rating
                 -> {context.update(INSTRUCTORS)
                        .set(INSTRUCTORS.RMP_RATING, rating.rating)
                        .set(INSTRUCTORS.RMP_TID, rating.rmpTeacherId)
                        .where(INSTRUCTORS.ID.eq(rating.instructorId))
                        .execute();
                      rating.reviews.forEach(review -> context.insertInto(REVIEWS, REVIEWS.INSTRUCTOR_ID, REVIEWS.REVIEW)
                      .values(rating.instructorId, review).execute());
                  });
  }
}
