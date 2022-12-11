package scraping;

import static utils.Nyu.*;

import java.util.*;
import java.util.function.*;
import utils.Try;

public abstract class TermScrapeResult implements Iterator<ArrayList<Course>> {
  protected final Consumer<ScrapeEvent> consumer;
  protected final Try ctx;
  protected final Term term;

  /* Real fucking stupid implementation of whatever you desire
   * to call this. Event listening, observer pattern, whatever.
   *
   *                  - Albert Liu, Nov 27, 2022 Sun 02:32
   */
  public static final class ScrapeEvent {
    public enum Kind {
      WARNING,
      MESSAGE,
      SUBJECT_START,
      PROGRESS,
      HINT_CHANGE;
    }

    public final Kind kind;
    public final String currentSubject;
    public final String message;
    public final int value;

    private ScrapeEvent(Kind kind, String currentSubject, String message,
                        int value) {
      this.kind = kind;
      this.currentSubject = currentSubject;
      this.message = message;
      this.value = value;
    }

    static ScrapeEvent warning(String subject, String message) {
      return new ScrapeEvent(Kind.WARNING, subject, message, 0);
    }

    static ScrapeEvent message(String subject, String message) {
      return new ScrapeEvent(Kind.MESSAGE, subject, message, 0);
    }

    static ScrapeEvent subject(String subject) {
      return new ScrapeEvent(Kind.SUBJECT_START, subject, "Fetching " + subject,
                             0);
    }

    static ScrapeEvent progress(int progress) {
      return new ScrapeEvent(Kind.PROGRESS, null, null, progress);
    }

    static ScrapeEvent hintChange(int hint) {
      return new ScrapeEvent(Kind.HINT_CHANGE, null, null, hint);
    }
  }

  public TermScrapeResult(Term term, Consumer<ScrapeEvent> consumer, Try ctx) {
    consumer = Objects.requireNonNullElse(consumer, e -> {});

    this.ctx = ctx;
    this.term = term;
    this.consumer = consumer;
  }

  public abstract ArrayList<School> getSchools();

  // @Note: This happens to allow JSON serialization of this object to
  // correctly run scraping, by forcing the serialization of the object to
  // run this method, which then consumes the iterator. It's stupid.
  //
  //                                  - Albert Liu, Nov 10, 2022 Thu 22:21
  public final ArrayList<Course> getCourses() {
    var courses = new ArrayList<Course>();
    while (this.hasNext()) {
      courses.addAll(this.next());
    }

    return courses;
  }
}
