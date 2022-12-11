package scraping;

import java.util.function.Consumer;
import me.tongfei.progressbar.ProgressBar;
import org.slf4j.Logger;

/* Real fucking stupid implementation of whatever you desire
 * to call this. Event listening, observer pattern, whatever.
 *
 *                  - Albert Liu, Nov 27, 2022 Sun 02:32
 */
public final class ScrapeEvent {
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

  public static Consumer<ScrapeEvent> cliConsumer(Logger logger,
                                                  ProgressBar bar) {
    return e -> {
      switch (e.kind) {
      case MESSAGE:
      case SUBJECT_START:
        bar.setExtraMessage(e.message);
        break;
      case WARNING:
        logger.warn(e.message);
        break;
      case PROGRESS:
        bar.stepBy(e.value);
        break;
      case HINT_CHANGE:
        bar.maxHint(e.value);
        break;
      }
    };
  }

  public static Consumer<ScrapeEvent> logConsumer(Logger logger) {
    return e -> {
      switch (e.kind) {
      case MESSAGE:
      case SUBJECT_START:
        logger.info(e.message);
        break;
      case WARNING:
        logger.warn(e.message);
        break;
      case PROGRESS:
      case HINT_CHANGE:
        break;
      }
    };
  }
}
