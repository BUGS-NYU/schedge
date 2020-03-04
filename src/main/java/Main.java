import cli.Schedge;
import database.GetConnection;
import scraping.query.GetClient;

public class Main {
  public static void main(String[] args) {
      try {
          new Schedge(args);
      } catch (Exception e) {
          GetConnection.close();
          GetClient.close();
          throw e;
      }
      GetConnection.close();
      GetClient.close();
  }
}
