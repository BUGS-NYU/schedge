package scraping.models;

import java.util.List;

public final class Rating {
  //  timestamp will be add later
  public final int instructorId;
  public final Integer rmpTeacherId;
  public final Integer ratingId;
  public final float rating;
  public final float helpful;
  public final String comment;
  public final int page;

  public Rating(int instructorId, Integer rmpTeacherId, Integer ratingId,
                float rating, float helpful, String comment, int page) {
    this.instructorId = instructorId;
    this.rmpTeacherId = rmpTeacherId;
    this.rating = rating;
    this.helpful = helpful;
    this.ratingId = ratingId;
    this.comment = comment;
    this.page = page;
  }

  public String toString() {
    return "URL: " + this.rmpTeacherId + ", Rating: " + this.rating +
        ", Comment: " + this.comment;
  }
}
