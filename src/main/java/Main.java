import cli.Schedge;
import database.GetConnection;
import scraping.GetRatings;
import scraping.query.GetClient;

public class Main {
  public static void main(String[] args) {
      try {
          new Schedge(args);
      } catch (Exception e) {
          e.printStackTrace();
      } finally {
          GetConnection.close();
          GetClient.close();
      }
  }
}
