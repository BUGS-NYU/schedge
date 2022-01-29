package utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Void;
import java.util.Arrays;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

public final class TryCatch {

  Logger logger;
  String message;
  Object[] params;

  private TryCatch(Logger logger, String message, Object[] params) {
    this.logger = logger;
    this.message = message;
    this.params = params;
  }

  // ---------------------------------------------------------------------------
  // INTERFACES
  // ---------------------------------------------------------------------------

  public interface MultiCallable<E> {
    E call(Object[] params) throws Exception;
  }

  public interface CallRunnable extends MultiCallable<Void> {
    void run() throws Exception;

    default Void call(Object[] params) throws Exception {
      this.run();
      return null;
    }
  }

  public interface CallSupplier<E> extends MultiCallable<E> {
    E get() throws Exception;

    default E call(Object[] params) throws Exception { return this.get(); }
  }

  public interface CallBoolSupplier extends MultiCallable<Boolean> {
    boolean get() throws Exception;

    default Boolean call(Object[] params) throws Exception {
      return this.get();
    }
  }

  public interface Procable2<P1, P2> extends MultiCallable<Void> {
    void call2(P1 p1, P2 p2) throws Exception;

    @SuppressWarnings("unchecked")
    default Void call(Object[] params) throws Exception {
      if (params.length != 2)
        throw new IllegalArgumentException(
            "wrong number of arguments (this is a bug in utils.TryCatch)");

      this.call2((P1)params[0], (P2)params[1]);
      return null;
    }
  }

  public interface Procable3<P1, P2, P3> extends MultiCallable<Void> {
    void call3(P1 p1, P2 p2, P3 p3) throws Exception;

    @SuppressWarnings("unchecked")
    default Void call(Object[] params) throws Exception {
      if (params.length != 3)
        throw new IllegalArgumentException(
            "wrong number of arguments (this is a bug in utils.TryCatch)");

      this.call3((P1)params[0], (P2)params[1], (P3)params[2]);
      return null;
    }
  }

  public interface Callable1<P1, E> extends MultiCallable<E> {
    E call1(P1 p) throws Exception;

    @SuppressWarnings("unchecked")
    default E call(Object[] params) throws Exception {
      if (params.length != 1)
        throw new IllegalArgumentException(
            "wrong number of arguments (this is a bug in utils.TryCatch)");

      return this.call1((P1)params[0]);
    }
  }

  public interface Callable2<P1, P2, E> extends MultiCallable<E> {
    E call2(P1 p1, P2 p2) throws Exception;

    @SuppressWarnings("unchecked")
    default E call(Object[] params) throws Exception {
      if (params.length != 2)
        throw new IllegalArgumentException(
            "wrong number of arguments (this is a bug in utils.TryCatch)");

      return this.call2((P1)params[0], (P2)params[1]);
    }
  }

  public interface Callable3<P1, P2, P3, E> extends MultiCallable<E> {
    E call3(P1 p1, P2 p2, P3 p3) throws Exception;

    @SuppressWarnings("unchecked")
    default E call(Object[] params) throws Exception {
      if (params.length != 3)
        throw new IllegalArgumentException(
            "wrong number of arguments (this is a bug in utils.TryCatch)");

      return this.call3((P1)params[0], (P2)params[1], (P3)params[2]);
    }
  }

  public interface Callable4<P1, P2, P3, P4, E> extends MultiCallable<E> {
    E call4(P1 p1, P2 p2, P3 p3, P4 p4) throws Exception;

    @SuppressWarnings("unchecked")
    default E call(Object[] params) throws Exception {
      if (params.length != 4)
        throw new IllegalArgumentException(
            "wrong number of arguments (this is a bug in utils.TryCatch)");

      return this.call4((P1)params[0], (P2)params[1], (P3)params[2],
                        (P4)params[3]);
    }
  }

  // ---------------------------------------------------------------------------
  // INSTANCE METHODS
  // ---------------------------------------------------------------------------

  public static TryCatch tc(Logger logger, String message, Object... params) {
    return new TryCatch(logger, message, params);
  }

  public <E> E pass(CallSupplier<E> supplier) {
    try {
      return supplier.get();
    } catch (Exception e) {
      this.logger.warn(this.message, this.params);
      throw new RuntimeException(e);
    }
  }

  public <E> E log(CallSupplier<E> supplier) {
    try {
      return supplier.get();
    } catch (Exception e) {
      this.logger.warn(this.message, this.params);
      return null;
    }
  }

  public void output() { this.logger.warn(this.message, this.params); }

  // ---------------------------------------------------------------------------
  // STATIC METHODS
  // ---------------------------------------------------------------------------

  public static <E> E tcNonnull(E e) {
    if (e == null)
      throw new NullPointerException();

    return e;
  }

  public static <E> E tcIgnore(CallSupplier<E> callable) {
    try {
      return callable.get();
    } catch (Exception e) {
      return null;
    }
  }

  public static void tcIgnore(CallRunnable callable) {
    try {
      callable.run();
    } catch (Exception e) {
    }
  }

  // @TODO java fails to compile if I change this to a tcLog overload and use it
  // like in actions/UpdateData.java. The complaint is that the overload is
  // ambiguous.
  //
  // However, it shouldn't be ambiguous, since the return value of
  // one overload interface is void and the other is a type variable, and
  // there's no conversion from E -> void
  @SuppressWarnings("unchecked")
  public static <P1> void tcLogVoid(Logger logger, Consumer<P1> callable,
                                    P1 p1) {
    _tcLog(logger, (p) -> {
      callable.accept((P1)p[0]);
      return null;
    }, p1);
  }

  public static <P1, P2> void
  tcLogVoid(Logger logger, Procable2<P1, P2> callable, P1 p1, P2 p2) {
    _tcLog(logger, callable, p1, p2);
  }

  public static <P1, P2, P3> void tcLogVoid(Logger logger,
                                            Procable3<P1, P2, P3> callable,
                                            P1 p1, P2 p2, P3 p3) {
    _tcLog(logger, callable, p1, p2, p3);
  }

  public static <P1, E> E tcLog(Logger logger, Callable1<P1, E> callable,
                                P1 p1) {
    return _tcLog(logger, callable, p1);
  }

  public static <P1, P2, E>
      E tcLog(Logger logger, Callable2<P1, P2, E> callable, P1 p1, P2 p2) {
    return _tcLog(logger, callable, p1, p2);
  }

  public static <P1, P2, P3, E> E tcLog(Logger logger,
                                        Callable3<P1, P2, P3, E> callable,
                                        P1 p1, P2 p2, P3 p3) {
    return _tcLog(logger, callable, p1, p2, p3);
  }

  public static <E> E tcLog(Logger logger, CallSupplier<E> callable,
                            Object... params) {
    return _tcLog(logger, callable, params);
  }

  public static void tcLog(Logger logger, CallRunnable callable,
                           Object... params) {
    _tcLog(logger, callable, params);
  }

  private static <E> E _tcLog(Logger logger, MultiCallable<E> callable,
                              Object... params) {
    try {
      return callable.call(params);
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      String exceptionPrintout = sw.toString();
      StackTraceElement[] trace = Thread.currentThread().getStackTrace();
      String message = "Exception from " + trace[3].getFileName() + ":" +
                       trace[3].getLineNumber() + " for params={}";

      logger.warn(message, Arrays.asList(params));
      logger.warn("Exception was: {}", exceptionPrintout);
      return null;
    }
  }

  public static void tcPass(CallRunnable run) {
    try {
      run.run();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void tcPass(CallRunnable run, String message) {
    try {
      run.run();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <E> E tcPass(CallSupplier<E> callable, String message) {
    return _tcPass(callable, message);
  }

  public static <P1, E> E tcPass(Callable1<P1, E> callable, String message,
                                 P1 p1) {
    return _tcPass(callable, message, p1);
  }

  public static <P1, P2, E>
      E tcPass(Logger logger, Callable2<P1, P2, E> callable, P1 p1, P2 p2) {
    return _tcPass(logger, callable, p1, p2);
  }

  private static <E> E _tcPass(MultiCallable<E> callable, String message,
                               Object... params) {
    try {
      return callable.call(params);
    } catch (Exception e) {
      message += "(with params " + Arrays.asList(params) + ")";
      throw new RuntimeException(message, e);
    }
  }

  private static <E> E _tcPass(Logger logger, MultiCallable<E> callable,
                               Object... params) {
    try {
      return callable.call(params);
    } catch (Exception e) {
      StackTraceElement[] trace = Thread.currentThread().getStackTrace();
      String message = "Exception from " + trace[3].getFileName() + ":" +
                       trace[3].getLineNumber() + " for params={}";

      logger.warn(message, Arrays.asList(params));
      throw new RuntimeException(e);
    }
  }

  public static <E> E tcFatal(CallSupplier<E> supplier, String format,
                              Object... obj) {
    try {
      return supplier.get();
    } catch (Exception e) {
      String message = MessageFormatter.arrayFormat(format, obj).getMessage();
      throw new Error(message, e);
    }
  }

  public static <E> E tcFatal(CallSupplier<E> supplier) {
    try {
      return supplier.get();
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  public static void tcAssert(CallBoolSupplier supplier) {
    tcAssert(supplier, "Assertion failed");
  }

  public static void tcAssert(CallBoolSupplier supplier, String message) {
    boolean check;

    try {
      check = supplier.get();
    } catch (Exception e) {
      throw new RuntimeException(message, e);
    }

    if (!check)
      throw new RuntimeException(message);
  }

  public static <P1> Consumer<P1>
  tcCreateAssert(Callable1<P1, Boolean> callable) {
    return tcCreateAssert(callable, "Assertion Failed");
  }

  public static <P1> Consumer<P1>
  tcCreateAssert(Callable1<P1, Boolean> callable, String message) {
    return (p1) -> {
      boolean check;

      try {
        check = callable.call1(p1);
      } catch (Exception e) {
        throw new RuntimeException(message, e);
      }

      if (!check)
        throw new RuntimeException(message);
    };
  }

  public static void tcFatal(CallRunnable run, String message) {
    try {
      run.run();
    } catch (Exception e) {
      throw new Error(message, e);
    }
  }

  public static void tcFatal(CallRunnable run) {
    try {
      run.run();
    } catch (Exception e) {
      throw new Error(e);
    }
  }
}
