package api.v2;

public class ApiError {
  private String message;
  public ApiError(String message) { this.message = message; }
  public String getMessage() { return message; }
}
