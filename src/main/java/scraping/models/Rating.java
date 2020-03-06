package scraping.models;

public final class Rating {
  public final long teacherId;
  public final float rating;

  // For the sake of simplicity, forget mostHelpful for now
  public Rating(long teacherId, float rating) {
    this.teacherId = teacherId;
    this.rating = rating;
  }

  public String toString() {
    return "URL: " + this.teacherId + ", Rating: " + this.rating;
  }
}
