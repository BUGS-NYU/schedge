package scraping.models;

public final class Rating {
  public final String url;
  public final float rating;

  public Rating(String url, float rating) {
    this.url = url;
    this.rating = rating;
  }
}
