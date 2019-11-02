package cli

import io.javalin.Javalin
import com.github.ajalt.clikt.core.CliktCommand
import api.App

internal class Serve : CliktCommand(name = "serve") {
    override fun run(): Unit =
        App.run()
}
