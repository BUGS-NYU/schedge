package utils;

import java.util.*;
import java.util.function.*;
import org.slf4j.*;

import javax.swing.text.html.Option;

public final class Try extends HashMap<String, Object> {
  public static Logger DEFAULT_LOGGER = LoggerFactory.getLogger("schedge");

  public interface Handler {
    void accept(Exception e);
  }

  public interface Call<T, E extends Exception> {
    T get() throws E;
  }

  public interface CallVoid<E extends Exception> {
    void get() throws E;
  }

  public final Logger logger;
  public Handler handler;

  private Try(Logger logger) {
    super();

    this.logger = logger;
  }

  public static Try Ctx() {
    return new Try(DEFAULT_LOGGER);
  }

  public static Try Ctx(Logger logger) {
    return new Try(logger);
  }

  public void handle(Exception e) {
    if (handler == null) {
      logger.error("Context: " + this, e);
      return;
    }

    handler.accept(e);
  }

  public <T, E extends Exception> T run(Call<T, E> supplier) {
    try {
      return supplier.get();
    } catch (RuntimeException e) {
      this.handle(e);
      throw e;
    } catch (Exception e) {
      this.handle(e);
      throw new RuntimeException(e);
    }
  }

  public <T, E extends Exception> Optional<T> retryRuntimeErrors(
      int retryCount, Call<T, E> supplier) throws E {
    for (int i = 0; i < retryCount; i++) {
      try {
        return Optional.of(supplier.get());
      } catch (RuntimeException e) {
        this.handle(e);
      }
    }

    return Optional.empty();
  }

  public static void tcPass(CallVoid supplier) {
    tcPass(
            () -> {
              supplier.get();
              return null;
            });
  }

  public static <E> E tcPass(Call<E, ? extends Exception> supplier) {
    try {
      return supplier.get();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void tcIgnore(CallVoid<? extends Exception> supplier) {
    try {
      supplier.get();
    } catch (Exception e) {
    }
  }

  public static <E> Optional<E> tcIgnore(Call<E, ? extends Exception> supplier) {
    try {
      return Optional.of( supplier.get());
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
