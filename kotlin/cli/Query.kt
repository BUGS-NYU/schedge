package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import models.Semester
import models.Term
import mu.KotlinLogging
import org.apache.http.HttpRequest
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import java.io.IOException

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
        // private val logger = KotlinLogging.logger("query.catalog")
        private val term: Term = Term(Semester.Summer, 2019)
        private val school: String = ""
        private val subject: String = ""
        private val catalogNumber: Int? = null
        private val keyword: String? = null
        private val classNumber: Int? = null
        private val location: String? = null

        // TODO Add this from Parser.parseCourse
        override fun run() {
            val client = AlbertClient()
            val params = mutableListOf( // URL params
                BasicNameValuePair("CSRFToken", client.csrfToken),
                BasicNameValuePair("term", term.id.toString()),
                BasicNameValuePair("acad_group", school),
                BasicNameValuePair("subject", subject),
                BasicNameValuePair("catalog_nbr", catalogNumber?.toString() ?: ""),
                BasicNameValuePair("keyword", keyword ?: ""),
                BasicNameValuePair("class_nbr", classNumber?.toString() ?: ""),
                BasicNameValuePair("nyu_location", location ?: "")
            )

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
        private val term: Term = Term(Semester.Summer, 2019)
        private val registrationNumber : Int = 12

        // TODO Add this from Parser.parseSection
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