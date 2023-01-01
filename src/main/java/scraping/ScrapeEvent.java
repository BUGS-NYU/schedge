package scraping;

import java.util.function.*;
import me.tongfei.progressbar.*;
import org.slf4j.*;

/* Real fucking stupid implementation of whatever you desire
 * to call this. Event listening, observer pattern, whatever.
 *
 *                  - Albert Liu, Nov 27, 2022 Sun 02:32
 */
public sealed class ScrapeEvent permits ScrapeEvent.Message,
    ScrapeEvent.Progress, ScrapeEvent.HintChange {
  public static sealed class Message extends ScrapeEvent permits Warn {
    public final String message;

    private Message(String currentSubject, String message) {
      super(currentSubject);
      this.message = message;
    }
  }

  public static final class Warn extends Message {
    private Warn(String currentSubject, String message) {
      super(currentSubject, message);
    }
  }

  public static final class Progress extends ScrapeEvent {
    public final int progress;
    private Progress(int progress) {
      super(null);
      this.progress = progress;
    }
  }

  public static final class HintChange extends ScrapeEvent {
    public final int newValue;
    private HintChange(int newValue) {
      super(null);
      this.newValue = newValue;
    }
  }

  public final String currentSubject;

  private ScrapeEvent(String currentSubject) {
    this.currentSubject = currentSubject;
  }

  static ScrapeEvent warning(String subject, String message) {
    return new Warn(subject, message);
  }

  static ScrapeEvent message(String subject, String message) {
    return new Message(subject, message);
  }

  static ScrapeEvent subject(String subject) {
    return new Message(subject, "Fetching " + subject);
  }

  static ScrapeEvent progress(int progress) {
    return new Progress(progress);
  }

  static ScrapeEvent hintChange(int hint) {
    return new HintChange(hint);
  }

  public static Consumer<ScrapeEvent> cli(Logger logger, ProgressBar bar) {
    return e -> {
      if (e instanceof Warn w) {
        logger.warn(w.message);
      } else if (e instanceof Message m) {
        bar.setExtraMessage(m.message);
      } else if (e instanceof Progress p) {
        bar.stepBy(p.progress);
      } else if (e instanceof HintChange h) {
        bar.maxHint(h.newValue);
      }
    };
  }

  public static Consumer<ScrapeEvent> log(Logger logger) {
    return e -> {
      if (e instanceof Warn w) {
        logger.warn(w.message);
      } else if (e instanceof Message m) {
        logger.info(m.message);
      }
    };
  }
}
