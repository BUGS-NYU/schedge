package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import mu.KotlinLogging

// TODO Change this to package-level protected if that becomes a thing
internal class Query : CliktCommand(name = "query") {
    companion object {
        const val ROOT_URL = "https://m.albert.nyu.edu/app/catalog/classSearch"
        const val DATA_URL = "https://m.albert.nyu.edu/app/catalog/getClassSearch"
        const val CATALOG_URL = "https://m.albert.nyu.edu/app/catalog/classsection/NYUNV"
    }

    init {
        this.subcommands(Catalog(), Section())
    }

    override fun run() = Unit

    private class Catalog : CliktCommand(name = "catalog") {
        private val logger = KotlinLogging.logger("query.catalog")

        // TODO Add this from Parser.parseCourse
        override fun run() = Unit
    }

    private class Section : CliktCommand(name = "section") {
        private val logger = KotlinLogging.logger("query.section")

        // TODO Add this from Parser.parseSection
        override fun run() = Unit
    }
}