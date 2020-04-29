package cli;

import api.v1.models.Course;
import cli.templates.LoginMixin;
import cli.templates.OutputFileMixin;
import cli.templates.RegistrationNumberMixin;
import cli.templates.TermMixin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import nyu.Term;
import nyu.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import register.*;

@CommandLine.
Command(name = "shop", synopsisSubcommandLabel = "(add | remove | enroll)")
public class Shop implements Runnable {
  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;

  private static Logger logger = LoggerFactory.getLogger("cli.Shop");

  @Override
  public void run() {
    throw new CommandLine.ParameterException(spec.commandLine(),
                                             "Missing required subcommand");
  }

  @CommandLine.
  Command(name = "add", sortOptions = false, headerHeading = "Usage:%n%n",
          synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n",
          header = "Adding course to shopping cart",
          description = "Adding one course to the shopping cart")
  public void
  addToCart(@CommandLine.Mixin TermMixin termMixin,
            @CommandLine.Mixin LoginMixin loginMixin,
            @CommandLine.Mixin RegistrationNumberMixin registrationNumberMixin,
            @CommandLine.Mixin OutputFileMixin outputFileMixin) {
    long start = System.nanoTime();
    Term term = termMixin.getTerm();
    List<Integer> sectionsRelated = new ArrayList<>();
    sectionsRelated.add(8608);
    sectionsRelated.add(10529);
    RegistrationCourse course1 = new RegistrationCourse(
          8587, sectionsRelated, "Y", 0, (float) 4.0
    );
////    RegistrationCourse course2 = new RegistrationCourse(
////            10009, sectionsRelated, "Y", 0, (float) 4.0
////    );
//    List<RegistrationCourse> lists = new ArrayList<>();
//    lists.add(course1);
////    lists.add(course2);
////    AddToCart.addToCart(loginMixin.getUser(), term,
////                    lists);
//    try {
//      AddToCart.addToCart(loginMixin.getUser(), term,
//              course1, Context.getContextAsync(term).get());
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    } catch (ExecutionException e) {
//      e.printStackTrace();
//    }
    //    if (registrationNumberMixin.getRegistrationNumber() != null) {
//      try {
//        AddToCart.addToCart(loginMixin.getUser(), term,
//                            registrationNumberMixin.getRegistrationNumber(),
//                            Context.getContextAsync(term).get());
//      } catch (InterruptedException | ExecutionException e) {
//        e.printStackTrace();
//      }
//    } else {
//      AddToCart.addToCart(loginMixin.getUser(), term,
//                          registrationNumberMixin.getRegistrationNumbers());
//    }
    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info(duration + " seconds");
  }

  @CommandLine.
  Command(name = "remove", sortOptions = false, headerHeading = "Usage:%n%n",
          synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n",
          header = "remove a course from the shopping cart",
          description = "remove course from the shopping cart")
  public void
  remove(@CommandLine.Mixin TermMixin termMixin,
         @CommandLine.Mixin LoginMixin loginMixin,
         @CommandLine.Mixin RegistrationNumberMixin registrationNumberMixin,
         @CommandLine.Mixin OutputFileMixin outputFileMixin) {
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

  @CommandLine.
  Command(name = "enroll", sortOptions = false, headerHeading = "Usage:%n%n",
          synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n", header = "Enroll courses",
          description = "Enrolling courses")
  public void
  enroll(@CommandLine.Mixin TermMixin termMixin,
         @CommandLine.Mixin LoginMixin loginMixin,
         @CommandLine.Mixin RegistrationNumberMixin registrationNumberMixin,
         @CommandLine.Mixin OutputFileMixin outputFileMixin) {
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
