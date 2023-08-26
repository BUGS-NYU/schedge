package utils;

import java.util.*;
import org.slf4j.*;

public final class Try extends HashMap<String, Object> {
  public static Logger DEFAULT_LOGGER = LoggerFactory.getLogger("schedge");

  public interface Call<T, E extends Exception> {
    T get() throws E;
  }

  public interface CallVoid<E extends Exception> {
    void get() throws E;
  }

  public final Logger logger;

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

  public <T, E extends Exception> T log(Call<T, E> supplier) {
    try {
      return supplier.get();
    } catch (RuntimeException e) {
      logger.error("Context: " + this, e);
      throw e;
    } catch (Exception e) {
      logger.error("Context: " + this, e);
      throw new RuntimeException(e);
    }
  }

  public static void tcPass(CallVoid<? extends Exception> supplier) {
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
    } catch (Exception ignored) {
    }
  }

  public static <E> Optional<E> tcIgnore(Call<E, ? extends Exception> supplier) {
    try {
      return Optional.of(supplier.get());
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
