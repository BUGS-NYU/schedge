import java.io.File

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