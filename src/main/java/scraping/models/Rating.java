package scraping.models;

import java.util.List;

public final class Rating {
  //  timestamp will be add later
  public final int instructorId;
  public final Integer rmpTeacherId;
  public final Integer ratingId;
  public final float rating;
  public final String comment;
  public final int page;

  // For the sake of simplicity, forget mostHelpful for now
  public Rating(int instructorId, Integer rmpTeacherId, float rating) {
    this.instructorId = instructorId;
    this.rmpTeacherId = rmpTeacherId;
    this.rating = rating;
    this.ratingId = null;
    this.comment = "";
    this.page = 1;
  }

  public Rating(int instructorId, Integer rmpTeacherId, int page) {
    this.instructorId = instructorId;
    this.rmpTeacherId = rmpTeacherId;
    this.page = page;
    this.rating = 0;
    this.ratingId = null;
    this.comment = "";
  }

  public Rating(int instructorId, Integer rmpTeacherId, Integer ratingId, float rating, String comment, int page) {
    this.instructorId = instructorId;
    this.rmpTeacherId = rmpTeacherId;
    this.rating = rating;
    this.ratingId = ratingId;
    this.comment = comment;
    this.page = page;
  }

  public String toString() {
    return "URL: " + this.rmpTeacherId + ", Rating: " + this.rating + ", Comment: " + this.comment;
  }
}
