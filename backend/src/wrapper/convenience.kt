package schedge.wrapper

fun <E, T> makePair(e: E, t: T): KtPair<E, T> {
  return KtPair(e, t);
}

public data class KtPair<E, T>(val component1: E, val component2: T) {

  public override fun toString(): String {
    return "(${component1}, ${component2})"

  }
}


