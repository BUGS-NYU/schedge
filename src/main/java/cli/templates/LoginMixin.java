package cli.templates;

import nyu.User;
import picocli.CommandLine;

public final class LoginMixin {

  private LoginMixin() {}
  @CommandLine.
  Option(names = "--username", description = "Be correct of capitalization")
  private String username;
  @CommandLine.
  Option(names = "--pwd", description = "Be correct of capitalization")
  private String password;

  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;

  public User getUserNotNull() {
    if (username == null || password == null) {
      return null;
    }
    return new User(username, password);
  }
  public User getUser() {
    User user = getUserNotNull();
    if (user == null) {
      throw new CommandLine.ParameterException(
          spec.commandLine(),
          "Must provide --username AND --pwd");
    }
    return user;
  }
}
