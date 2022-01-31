package types;

public class User {
  public final String username;
  public final String password;

  public User(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public String toString() {
    return String.format("User(username=%s,password=%s)", username, password);
  }
}
