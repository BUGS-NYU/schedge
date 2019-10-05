import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.default
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import database.DatabaseManager
import java.io.IOException

class App() : CliktCommand(invokeWithoutSubcommand = true) {
    private val logLevel by option(help = "Set the logging level.")
        .switch(
            "--debug" to Logging.DEBUG,
            "--info" to Logging.INFO,
            "--warn" to Logging.WARN,
            "--error" to Logging.ERROR
        ).default(Logging.WARN)

    private val logger : Logging
      get() {
        return Logging.getLogger(logLevel)
      }

    override fun run() {
        DatabaseManager.setupDatabase()
        throw logger.error("Nothing's been implemented!")
    }
}

fun main(args: Array<String>) {
    for (str in args) {
        println(str)
    }
    App().main(args)
}
