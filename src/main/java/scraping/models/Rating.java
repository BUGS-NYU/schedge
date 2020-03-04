package scraping.models;

public final class Rating {
  public final String url;
  public final float rating;
  public final String mostHelpful;

  public Rating(String url, float rating, String mostHelpful) {
    this.url = url;
    this.rating = rating;
    this.mostHelpful = mostHelpful;
  }

  // For the sake of simplicity, forget mostHelpful for now
  public Rating(String url, float rating) {
    this.url = url;
    this.rating = rating;
    this.mostHelpful = "";
  }
}
