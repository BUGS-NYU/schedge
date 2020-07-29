package utils;

import java.util.*;

public class PolyFill {

  public static <E> List<E> listOf(E e) {
    ArrayList<E> list = new ArrayList<E>();
    list.add(e);
    return list;
  }
}
