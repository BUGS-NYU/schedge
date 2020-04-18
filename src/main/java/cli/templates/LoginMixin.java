package cli.templates;

import java.io.Console;
import nyu.User;
import picocli.CommandLine;

public final class LoginMixin {

  private LoginMixin() {}
  @CommandLine.Option(names = "--username") private String username;

  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;

  public User getUserNotNull() {
    if (username == null) {
      return null;
    }
    Console cons;
    char[] passwd = null;
    if ((cons = System.console()) != null &&
        (passwd = cons.readPassword("[%s]", "Password:")) != null) {
      java.util.Arrays.fill(passwd, ' ');
    }
    return new User(username, String.valueOf(passwd));
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
