package schedge.wrapper

data class KtPair<E, T>(val component1: E, val component2: T) {

  override fun toString(): String {
    return "(${component1}, ${component2})"

  }
}


