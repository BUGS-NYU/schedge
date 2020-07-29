package scraping.models;

public final class Rating {
  public final int instructorId;
  public final Integer rmpTeacherId;
  public final float rating;

  // For the sake of simplicity, forget mostHelpful for now
  public Rating(int instructorId, Integer rmpTeacherId, float rating) {
    this.instructorId = instructorId;
    this.rmpTeacherId = rmpTeacherId;
    this.rating = rating;
  }

  public String toString() {
    return "URL: " + this.rmpTeacherId + ", Rating: " + this.rating;
  }
}
