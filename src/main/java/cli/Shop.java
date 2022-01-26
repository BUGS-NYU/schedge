package cli;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Mixin;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.Spec;

import java.util.List;
import java.util.concurrent.ExecutionException;
import nyu.Term;
import nyu.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import register.*;

@Command(name = "shop", synopsisSubcommandLabel = "(add | remove | enroll)")
public class Shop implements Runnable {
  @Spec private CommandLine.Model.CommandSpec spec;

  private static Logger logger = LoggerFactory.getLogger("cli.Shop");
  @Option(names = {"-h", "--help"}, usageHelp = true,
          description = "display a help message")
  boolean displayHelp;

  @Override
  public void run() {
    throw new CommandLine.ParameterException(spec.commandLine(),
                                             "Missing required subcommand");
  }

  @Command(
      name = "add", sortOptions = false, headerHeading = "Usage:%n%n",
      synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
      parameterListHeading = "%nParameters:%n",
      optionListHeading = "%nOptions:%n",
      header = "Adding course to shopping cart",
      description =
          "Adding one course to the shopping cart. "
          + "Inputting --courses command multiple time to inputting multiple"
          + "courses at once"
          +
          "Inputting as follows Eg: 1=3 -> {1=3} in mapping; 1=3,4,5 -> {1=3,4,5}, 1= -> empty list")
  public void
  addToCart(@Mixin Mixins.Term termMixin, @Mixin Mixins.Login loginMixin,
            @Mixin Mixins.CourseRegistration courseRegistrationMixin,
            @Mixin Mixins.OutputFile outputFileMixin) {
    long start = System.nanoTime();
    Term term = termMixin.getTerm();
    List<RegistrationCourse> courses = courseRegistrationMixin.convertCourses();
    AddToCart.addToCart(loginMixin.getUser(), term, courses);
    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info(duration + " seconds");
  }

  @Command(name = "remove", sortOptions = false, headerHeading = "Usage:%n%n",
           synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
           parameterListHeading = "%nParameters:%n",
           optionListHeading = "%nOptions:%n",
           header = "remove a course from the shopping cart",
           description = "remove course from the shopping cart")
  public void
  remove(@Mixin Mixins.Term termMixin, @Mixin Mixins.Login loginMixin,
         @Mixin Mixins.RegistrationNumber registrationNumberMixin,
         @Mixin Mixins.OutputFile outputFileMixin) {
    long start = System.nanoTime();
    Term term = termMixin.getTerm();
    try {
      EnrollCourses.removeFromCart(
          loginMixin.getUser(), term,
          registrationNumberMixin.getRegistrationNumbers(),
          Context.getContextAsync(term).get());
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info(duration + " seconds");
  }

  @Command(name = "enroll", sortOptions = false, headerHeading = "Usage:%n%n",
           synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
           parameterListHeading = "%nParameters:%n",
           optionListHeading = "%nOptions:%n", header = "Enroll courses",
           description = "Enrolling courses")
  public void
  enroll(@Mixin Mixins.Term termMixin, @Mixin Mixins.Login loginMixin,
         @Mixin Mixins.RegistrationNumber registrationNumberMixin,
         @Mixin Mixins.OutputFile outputFileMixin) {
    long start = System.nanoTime();
    Term term = termMixin.getTerm();
    User user = loginMixin.getUser();
    try {
      String data =
          EnrollCourses
              .enrollCourse(user, term,
                            registrationNumberMixin.getRegistrationNumbers(),
                            Context.getContextAsync(term).get())
              .get();
      ParseEnroll.parseRegistrationNumber(data);
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info(duration + " seconds");
  }
}
