package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

internal class Schedge() : CliktCommand(name = "schedge") {

    init {
        this.subcommands(Query(), Parse())
    }

    override fun run() = Unit
}


