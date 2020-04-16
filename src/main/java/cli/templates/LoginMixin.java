package cli.templates;

import java.util.Arrays;
import java.util.List;
import nyu.SubjectCode;
import nyu.User;
import picocli.CommandLine;

public final class LoginMixin {

  private LoginMixin() {}
  @CommandLine.Option(names = "--username") private String username;
  @CommandLine.
  Option(names = "--pwd", description = "subject code: CSCI, MATH, etc")
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
          "Must provide at least one. Either --term OR --semester AND --year");
    }
    return user;
  }
}
