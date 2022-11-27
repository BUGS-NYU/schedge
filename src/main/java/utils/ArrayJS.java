package utils;

import java.util.function.*;

public final class ArrayJS {
  public interface ArrayFunc2<T> {
    boolean run(T obj, int index);
  }

  public interface ArrayFunc<T> extends ArrayFunc2<T> {
    boolean run(T obj);

    default boolean run(T obj, int index) { return this.run(obj); }
  }

  public static <T> T find(Iterable<T> iterable, ArrayFunc<T> func) {
    return find(iterable, (ArrayFunc2<T>)func);
  }

  public static <T> T find(Iterable<T> iterable, ArrayFunc2<T> func) {
    int i = 0;
    for (var t : iterable) {
      if (func.run(t, i)) {
        return t;
      }

      i++;
    }

    return null;
  }

  public static <T> T run(Supplier<T> supplier) { return supplier.get(); }
}
