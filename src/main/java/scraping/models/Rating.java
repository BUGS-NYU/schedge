package scraping.models;

public final class Rating {
  public final long url;
  public final float rating;

  // For the sake of simplicity, forget mostHelpful for now
  public Rating(long url, float rating) {
    this.url = url;
    this.rating = rating;
  }

  public String toString() {
    return "URL: " + this.url + ", Rating: " + this.rating;
  }
}
