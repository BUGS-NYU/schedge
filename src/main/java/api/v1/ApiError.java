package api.v1;

public class ApiError {
  private String message;
  public ApiError(String message) { this.message = message; }
  public String getMessage() { return message; }
}
