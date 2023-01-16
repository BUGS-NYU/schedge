package utils;

import java.util.*;
import java.util.function.*;

public final class ArrayJS {
  public interface ArrayFunc<T> {
    boolean run(T obj);
  }

  public static <T> Optional<T> find(Iterable<T> iterable, ArrayFunc<T> func) {
    return findImpl(iterable, func);
  }

  private static <T> Optional<T> findImpl(Iterable<T> iterable, ArrayFunc<T> func) {
    for (var t : iterable) {
      if (func.run(t)) {
        return Optional.of(t);
      }
    }

    return Optional.empty();
  }

  public static <T, E extends Exception> T run(Try.Call<T, E> supplier) throws E {
    return supplier.get();
  }
}
