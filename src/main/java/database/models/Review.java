package database.models;

public class Review {
  public final float helpful;
  public final float rating;
  public final String comment;

  public Review(float helpful, float rating, String comment) {
    this.helpful = helpful;
    this.rating = rating;
    this.comment = comment;
  }

  public float getHelpful() { return helpful; }

  public float getRating() { return rating; }

  public String getComment() { return comment; }

  public String toString() {
    return "Rating: " + this.rating + " Helpful: " + this.helpful + "Comment: [" + comment + " ]";
  }
}