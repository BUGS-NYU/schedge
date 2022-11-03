package utils;

import java.io.*;
import java.lang.Void;
import java.util.*;
import java.util.Arrays;
import java.util.function.Consumer;
import org.slf4j.*;
import org.slf4j.helpers.MessageFormatter;

public final class Try extends HashMap<String, Object> {
  public static Logger DEFAULT_LOGGER = LoggerFactory.getLogger("schedge");

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

  public Try child() { return new Try(this, null); }

  public static void tcPass(CallVoid supplier) {
    try {
      supplier.get();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <E> E tcPass(Call<E> supplier) {
    try {
      return supplier.get();
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

  public void log(CallVoid supplier) {
    try {
      supplier.get();
    } catch (Exception e) {
      logger.warn(this.toString());
      throw new RuntimeException(e);
    }
  }

  public <E> E log(Call<E> supplier) {
    try {
      return supplier.get();
    } catch (Exception e) {
      logger.warn(this.toString());
      throw new RuntimeException(e);
    }
  }

  public void fatal(CallVoid supplier) {
    try {
      supplier.get();
    } catch (Exception e) {
      logger.error(this.toString());
      throw new Error(e);
    }
  }

  public <E> E fatal(Call<E> supplier) {
    try {
      return supplier.get();
    } catch (Exception e) {
      logger.error(this.toString());
      throw new Error(e);
    }
  }

  @Override
  public final String toString() {
    // @TODO: this code assumes that a context will only ever run
    // the toString method once; that's a wrong assumption, but
    // probably fine to make for as long as this project will live
    // on for.
    //
    // Additionally, the only consequence of breaking that assumption
    // is that some internal fields will be duplicated; this may
    // cause a weird memory leak in the future or something, but
    // it's not my problem right now so whatever.
    //
    //                        - Albert Liu, Nov 02, 2022 Wed 23:14

    var ctx = this.parent;
    while (ctx != null) {

      for (var entry : ctx.entrySet()) {
        this.computeIfAbsent(entry.getKey(), k -> entry.getValue());
      }

      ctx = ctx.parent;
    }

    return super.toString();
  }
}
