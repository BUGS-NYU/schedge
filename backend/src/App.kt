package schedge

import java.io.IOException
import schedge.models.Term
import schedge.models.Semester
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import schedge.Logging

class App(): CliktCommand() {
    private val loggingLevel by option(help="The logging level of the application.").int().default(Logging.WARN).validate {
        Logging.info("Logging level set to $it (${when (it) {
            1 -> "debug"
            2 -> "info"
            3 -> "warn"
            4 -> "error"
            else -> throw IOException("Logging level set to invalid value.")
        }})")
    }

    override fun run() = Unit
}

fun main(args: Array<String>) = App().main(args)
