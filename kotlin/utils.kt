fun String.asResourceLines(): List<String> {
    val resource = object {}::class.java.getResource(this)
    return resource.readText(Charsets.UTF_8).lineSequence().filter { it.isNotEmpty() }.toList()
}
