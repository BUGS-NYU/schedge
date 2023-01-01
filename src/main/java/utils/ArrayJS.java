package utils;

import java.util.function.*;

public final class ArrayJS {
  public interface ArrayFunc<T> {
    boolean run(T obj);
  }

  public static <T> T find(Iterable<T> iterable, ArrayFunc<T> func) {
    return findImpl(iterable, func);
  }

  private static <T> T findImpl(Iterable<T> iterable, ArrayFunc<T> func) {
    int i = 0;
    for (var t : iterable) {
      if (func.run(t)) {
        return t;
      }

      i++;
    }

    return null;
  }

  public static <T> T run(Supplier<T> supplier) {
    return supplier.get();
  }
}
