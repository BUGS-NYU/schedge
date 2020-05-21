package scraping.models;

import java.util.List;

public final class Rating {
  public final int instructorId;
  public final Integer rmpTeacherId;
  public final Float rating;
  public final List<String> reviews;

  // For the sake of simplicity, forget mostHelpful for now
  public Rating(int instructorId, Integer rmpTeacherId, Float rating, List<String> reviews) {
    this.instructorId = instructorId;
    this.rmpTeacherId = rmpTeacherId;
    this.rating = rating;
    this.reviews = reviews;
  }

  public String toString() {
    return "URL: " + this.rmpTeacherId + ", Rating: " + this.rating;
  }
}
