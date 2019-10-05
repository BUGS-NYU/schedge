import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.default
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import java.io.IOException

fun validateLoggingLevel(level: Int?) {
    val loggingLevel = level ?: Logging.WARN
    Logging.loggingLevel = loggingLevel
    val loggingLevelName = when (loggingLevel) {
        1 -> "debug"
        2 -> "info"
        3 -> "warn"
        4 -> "error"
        else -> throw IOException("Logging level set to invalid value (${level}).")
    }
    Logging.info("Logging level set to $level (${loggingLevelName})")
}

class App() : CliktCommand() {
    private val loggingLevel by mutuallyExclusiveOptions(
        option(help="Set the logging level.")
            .switch(
                "--debug" to Logging.DEBUG,
                "--info" to Logging.INFO,
                "--warn" to Logging.WARN,
                "--error" to Logging.ERROR
            ).also { validateLoggingLevel(it.value) },
        option("--loggingLevel", help="Set the logging level numerically.")
            .int().also { validateLoggingLevel(it.value) }
    )

    override fun run() = Unit
}

fun main(args: Array<String>) = App().main(args)
