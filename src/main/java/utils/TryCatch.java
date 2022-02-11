package utils;

import java.io.*;
import java.lang.Void;
import java.util.Arrays;
import java.util.function.Consumer;
import org.slf4j.*;
import org.slf4j.helpers.MessageFormatter;

public interface TryCatch {
  public static Logger DEFAULT_LOGGER = LoggerFactory.getLogger("schedge");

  public interface Call<E> { E get() throws Exception; }

  public interface CallVoid { void get() throws Exception; }

  void onError(Throwable e);

  default void fatal(CallVoid supplier) {
    try {
      supplier.get();
    } catch (Exception e) {
      this.onError(e);
      throw new Error(e);
    }
  }

  default void pass(CallVoid supplier) {
    try {
      supplier.get();
    } catch (Exception e) {
      this.onError(e);
      throw new RuntimeException(e);
    }
  }

  default void log(CallVoid supplier) {
    try {
      supplier.get();
    } catch (Exception e) {
      this.onError(e);
    }
  }

  default<E> E fatal(Call<E> supplier) {
    try {
      return supplier.get();
    } catch (Exception e) {
      this.onError(e);
      throw new Error(e);
    }
  }

  default<E> E pass(Call<E> supplier) {
    try {
      return supplier.get();
    } catch (Exception e) {
      this.onError(e);
      throw new RuntimeException(e);
    }
  }

  default<E> E log(Call<E> supplier) {
    try {
      return supplier.get();
    } catch (Exception e) {
      this.onError(e);
      return null;
    }
  }

  // ---------------------------------------------------------------------------
  // STATIC METHODS
  // ---------------------------------------------------------------------------

  public static TryCatch tcNew(Logger logger) {
    return e -> logger.error("", e);
  }

  public static TryCatch tcNew(Logger logger, String message,
                               Object... params) {
    if (params.length == 0) {
      return e -> { logger.warn(message, e); };
    }

    if (params.length == 1) {
      return e -> { logger.warn(message, params[0], e); };
    }

    Object[] errorParams = new Object[params.length + 1];

    for (int i = 0; i < params.length; i++)
      errorParams[i] = params;

    return e -> {
      errorParams[params.length] = e;
      logger.warn(message, errorParams);
    };
  }

  public static TryCatch tcNew(TryCatch t) { return t; }

  public static <E> E nonnull(E e) {
    if (e == null)
      throw new NullPointerException();

    return e;
  }

  public static void tcPass(CallVoid callable) {
    try {
      callable.get();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void tcIgnore(CallVoid callable) {
    try {
      callable.get();
    } catch (Exception e) {
    }
  }

  public static void tcLog(CallVoid callable, String format, Object... obj) {
    try {
      callable.get();
    } catch (Exception e) {
    }
  }

  public static void tcFatal(CallVoid run, String message) {
    try {
      run.get();
    } catch (Exception e) {
      throw new Error(message, e);
    }
  }

  public static <E> E tcPass(Call<E> callable) {
    try {
      return callable.get();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <E> E tcIgnore(Call<E> callable) {
    try {
      return callable.get();
    } catch (Exception e) {
      return null;
    }
  }

  public static <E> E tcFatal(Call<E> supplier, String format, Object... obj) {
    try {
      return supplier.get();
    } catch (Exception e) {
      String message = MessageFormatter.arrayFormat(format, obj).getMessage();
      throw new Error(message, e);
    }
  }

  public static <E> E tcFatal(Call<E> supplier) {
    try {
      return supplier.get();
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  public static void tcFatal(CallVoid run) {
    try {
      run.get();
    } catch (Exception e) {
      throw new Error(e);
    }
  }
}
