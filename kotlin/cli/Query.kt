package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import models.*
import mu.KotlinLogging
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.HttpClients
import java.io.IOException
import org.apache.http.message.BasicNameValuePair as KVPair

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

    private class Catalog() : CliktCommand(name = "catalog") {
        private val logger = KotlinLogging.logger("query.catalog")
        private val term: Term by option("--term").convert {
            Term.fromId(Integer.parseInt(it))
        }.required()
        private val school: School by option("--school").choice(*(
                Schools.map { Pair(it.abbrev, it) }
                        + Schools.map { Pair(it.abbrev.toLowerCase(), it) }
                ).toTypedArray()
        ).required()
        private val subject: Subject by option("--subject").choice(*(
                Subjects.map { Pair(it.abbrev, it) }
                        + Subjects.map { Pair(it.abbrev.toLowerCase(), it) }
                ).toTypedArray()
        ).required()
        private val catalogNumber: Int? by option("--catalog-number").int().restrictTo(0..Int.MAX_VALUE)
        private val keywords: String? by option("--keywords")
        private val classNumber: Int? by option("--class-number").int().restrictTo(0..Int.MAX_VALUE)
        private val location: String? by option("--location")

        override fun run() {
            val client = AlbertClient()
            logger.info { "Created client." }
            val params = mutableListOf( // URL params
                KVPair("CSRFToken", client.csrfToken),
                KVPair("term", term.id.toString()),
                KVPair("acad_group", school.abbrev),
                KVPair("subject", subject.abbrev),
                KVPair("catalog_nbr", catalogNumber?.toString() ?: ""),
                KVPair("keyword", keywords ?: ""),
                KVPair("class_nbr", classNumber?.toString() ?: ""),
                KVPair("nyu_location", location ?: "")
            )
            logger.debug { "Params are ${params}." }


            val request = HttpPost(DATA_URL).apply {
                entity = UrlEncodedFormEntity(params)
                addHeader("Referrer", "$ROOT_URL/${term.id}")
                addHeader("Host", "m.albert.nyu.edu")
            }
            println(client.execute(request))
        }
    }

    private class Section : CliktCommand(name = "section") {
        private val logger = KotlinLogging.logger("query.section")
        private val term: Term by option("--term").convert {
            Term.fromId(Integer.parseInt(it))
        }.required()
        private val registrationNumber: Int by option("--registration-number").int()
            .restrictTo(0..Int.MAX_VALUE).required()

        override fun run() {
            val client = AlbertClient()
            val request = HttpGet("$CATALOG_URL/${term.id}/${registrationNumber}")
            println(client.execute(request))
        }
    }

    private class AlbertClient() {
        private val logger = KotlinLogging.logger {}
        private val httpClient = HttpClients.custom().useSystemProperties().build()
        private val httpContext = HttpClientContext.create().apply {
            cookieStore = BasicCookieStore()
        }
        val csrfToken: String
            get() {
                val cookie = httpContext.cookieStore.cookies.find { cookie ->
                    cookie.name == "CSRFCookie"
                }

                if (cookie == null) {
                    logger.error {
                        val cookies = httpContext.cookieStore.cookies.map {
                            "${it.name}: \"${it.value}\""
                        }
                        "Couldn't find `CSRFCookie`. " +
                                "Cookies found were [\n  ${cookies.joinToString(",\n  ")}]."
                    }
                    throw IOException("NYU servers did something unexpected.")
                } else {
                    return cookie.value
                }
            }

        init {
            logger.debug("Creating scraper instance...")

            // Get a CSRF token for this scraper. This token allows us to get
            // data straight from NYU's internal web APIs.
            val response = httpClient.execute(HttpGet(ROOT_URL), httpContext)
            response.close()

            logger.info { "Scraper instance created with CSRF Token '${csrfToken}'." }
        }

        fun execute(req: HttpUriRequest): String {
            logger.info { "Executing ${req.method.toUpperCase()} request" }
            return this.httpClient.execute(req, this.httpContext).entity.content.bufferedReader().readText()
        }
    }
}
