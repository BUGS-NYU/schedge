import java.io.File
import java.util.*

fun String.asResourceLines(): List<String> {
    val resource = object {}::class.java.getResource(this)
    return resource.readText(Charsets.UTF_8).lineSequence().filter { it.isNotEmpty() }.toList()
}

fun String?.writeToFileOrStdout(text: Any) {
    return if (this == null) {
        println(text)
    } else {
        File(this).writeText(text.toString())
    }
}

fun String?.readFromFileOrStdin(): String {
    return if (this == null) {
        System.`in`.bufferedReader().readText()
    } else {
        File(this).bufferedReader().readText()
    }
}

fun <A, B, C> Pair<A, B>.mapFirst(lambda: (A) -> C): Pair<C, B> {
    return Pair(lambda(this.first), this.second)
}

fun <A, B, C> Pair<A, B>.mapSecond(lambda: (B) -> C): Pair<A, C> {
    return Pair(this.first, lambda(this.second))
}

// Taken with modifications from https://github.com/bijukunjummen/kfun/blob/master/src/main/kotlin/io/kfun/Tuples.kt
object Tuple {

    operator fun <A> invoke(_1: A): T.T1<A> = T.T1(_1)
    operator fun <A, B> invoke(_1: A, _2: B): T.T2<A, B> = T.T2(_1, _2)
    operator fun <A, B, C> invoke(_1: A, _2: B, _3: C): T.T3<A, B, C> = T.T3(_1, _2, _3)
    operator fun <A, B, C, D> invoke(_1: A, _2: B, _3: C, _4: D): T.T4<A, B, C, D> = T.T4(_1, _2, _3, _4)

}
sealed class T {

    data class T1<out A>(val _1: A) : T() {

        fun <_A> map1(lambda: (A) -> _A): T1<_A> {
            return T1(lambda(_1))
        }
    }

    data class T2<out A, out B>(val _1: A, val _2: B) : T() {

        fun <_A> map1(lambda: (A) -> _A): T2<_A, B> {
            return T2(lambda(_1), _2)
        }

        fun <_B> map2(lambda: (B) -> _B): T2<A, _B> {
            return T2(_1, lambda(_2))
        }
    }

    data class T3<out A, out B, out C>(val _1: A, val _2: B, val _3: C) : T() {

        fun <_A> map1(lambda: (A) -> _A): T3<_A, B, C> {
            return T3(lambda(_1), _2, _3)
        }

        fun <_B> map2(lambda: (B) -> _B): T3<A, _B, C> {
            return T3(_1, lambda(_2), _3)
        }

        fun <_C> map3(lambda: (C) -> _C): T3<A, B, _C> {
            return T3(_1, _2, lambda(_3))
        }
    }

    data class T4<out A, out B, out C, out D>(val _1: A, val _2: B, val _3: C, val _4: D) : T() {

        fun <_A> map1(lambda: (A) -> _A): T4<_A, B, C, D> {
            return T4(lambda(_1), _2, _3, _4)
        }

        fun <_B> map2(lambda: (B) -> _B): T4<A, _B, C, D> {
            return T4(_1, lambda(_2), _3, _4)
        }

        fun <_C> map3(lambda: (C) -> _C): T4<A, B, _C, D> {
            return T4(_1, _2, lambda(_3), _4)
        }

        fun <_D> map4(lambda: (D) -> _D): T4<A, B, C, _D> {
            return T4(_1, _2, _3, lambda(_4))
        }
    }
}

