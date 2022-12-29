package utils;

import java.util.*;
import org.slf4j.*;

public final class Try extends HashMap<String, Object> {
  public static Logger DEFAULT_LOGGER = LoggerFactory.getLogger("schedge");

  private final class TryCounter {
    int value = 0;

    @Override
    public String toString() {
      return "" + value;
    }
  }

  public interface Call<E> {
    E get() throws Exception;
  }

  public interface CallVoid {
    void get() throws Exception;
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

  public static void tcPass(CallVoid supplier) {
    tcPass(() -> {
      supplier.get();
      return null;
    });
  }

  public static <E> E tcPass(Call<E> supplier) {
    try {
      return supplier.get();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void tcIgnore(CallVoid supplier) {
    try {
      supplier.get();
    } catch (Exception e) {
    }
  }

  public static <E> E tcIgnore(Call<E> supplier) {
    try {
      return supplier.get();
    } catch (Exception e) {
      return null;
    }
  }

  public void increment(String name) {
    increment(name, 1);
  }
  public void increment(String name, int i) {
    var counter = (TryCounter)this.computeIfAbsent(name, k -> new TryCounter());
    counter.value += i;
  }

  public void log(CallVoid supplier) {
    log(() -> {
      supplier.get();
      return null;
    });
  }

  public <E> E log(Call<E> supplier) {
    try {
      return supplier.get();
    } catch (Throwable throwable) {
      logger.warn("Context: " + this);
      if (throwable instanceof RuntimeException e)
        throw e;
      if (throwable instanceof Error e)
        throw e;
      throw new RuntimeException(throwable);
    }
  }
}
