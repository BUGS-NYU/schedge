public class Main {
  public static void main(String[] args) {
    if(args.length == 0) {
      throw new IllegalArgumentException(
              "Please provide either command query, scrape, parse OR db");
    }

    new cli.schedge(args);
  }
}
