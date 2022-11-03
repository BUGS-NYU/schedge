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
  public final Try parent;

  private Try(Try parent, Logger logger) {
    super();

    this.parent = parent;
    this.logger = logger;
  }

  public static Try Ctx() { return new Try(null, DEFAULT_LOGGER); }
  public static Try Ctx(Logger logger) { return new Try(null, logger); }
  public Try child() { return new Try(this, null); }

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

  public void increment(String name) { increment(name, 1); }
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
    } catch (Throwable e) {
      logger.warn("Context: " + this.toString());
      if (e instanceof RuntimeException)
        throw(RuntimeException) e;
      if (e instanceof Error)
        throw(Error) e;
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toString() {
    // @TODO: this code assumes that a context will only ever run
    // the toString method once; that's a wrong assumption, but
    // probably fine to make for as long as this project will live
    // on for, since the consequence is limited to wasted memory.
    //
    //                        - Albert Liu, Nov 02, 2022 Wed 23:14
    for (var ctx = this.parent; ctx != null; ctx = ctx.parent) {
      for (var entry : ctx.entrySet()) {
        this.computeIfAbsent(entry.getKey(), k -> entry.getValue());
      }
    }

    return super.toString();
  }
}
