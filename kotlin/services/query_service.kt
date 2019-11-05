package services

import models.Subject
import models.Term
import mu.KLogger
import mu.KotlinLogging
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import java.io.IOException

fun queryCatalog(logger: KLogger, term: Term, subjects: Array<out Subject>): Sequence<Pair<Subject, String>> {
    logger.info { "querying catalog for term=$term multiple subjects..." }
    val client = AlbertClient()

    return sequenceOf(*subjects).map { subject ->
        logger.info { "Querying catalog with subject=$subject" }
        val params = mutableListOf( // URL params
            BasicNameValuePair("CSRFToken", client.csrfToken),
            BasicNameValuePair("term", term.id.toString()),
            BasicNameValuePair("acad_group", subject.school),
            BasicNameValuePair("subject", subject.abbrev)
        )
        logger.debug { "Params are ${params}." }

        val request = HttpPost(DATA_URL).apply {
            entity = UrlEncodedFormEntity(params)
            addHeader("Referrer", "${ROOT_URL}/${term.id}")
            addHeader("Host", "m.albert.nyu.edu")
        }

        val result = client.execute(request)
        if (result == "No classes found matching your criteria.") {
            throw IOException("No classes found matching criteria school=${subject.school}, subject=${subject.abbrev}")
        } else {
            Pair(subject, result)
        }
    }
}

// fun queryCatalog(
//     logger: KLogger,
//     term: Term,
//     subject: Subject,
//     catalogNumber: Int? = null,
//     keywords: String? = null,
//     classNumber: Int? = null,
//     location: String? = null
// ): String {
//     logger.info { "querying catalog with term=$term and subject=$subject" }
//     val client = AlbertClient()
//     val params = mutableListOf( // URL params
//         BasicNameValuePair("CSRFToken", client.csrfToken),
//         BasicNameValuePair("term", term.id.toString()),
//         BasicNameValuePair("acad_group", subject.school),
//         BasicNameValuePair("subject", subject.abbrev),
//         BasicNameValuePair("catalog_nbr", catalogNumber?.toString() ?: ""),
//         BasicNameValuePair("keyword", keywords ?: ""),
//         BasicNameValuePair("class_nbr", classNumber?.toString() ?: ""),
//         BasicNameValuePair("nyu_location", location ?: "")
//     )
//     logger.debug { "Params are ${params}." }
// 
// 
//     val request = HttpPost(DATA_URL).apply {
//         entity = UrlEncodedFormEntity(params)
//         addHeader("Referrer", "${ROOT_URL}/${term.id}")
//         addHeader("Host", "m.albert.nyu.edu")
//     }
//     return client.execute(request)
// }

private const val ROOT_URL = "https://m.albert.nyu.edu/app/catalog/classSearch"
private const val DATA_URL = "https://m.albert.nyu.edu/app/catalog/getClassSearch"

private class AlbertClient {
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
        logger.debug("Creating client instance...")

        // Get a CSRF token for this client. This token allows us to get
        // data straight from NYU's internal web APIs.
        val response = httpClient.execute(HttpGet(ROOT_URL), httpContext)
        response.close()

        logger.info { "Client instance created with CSRF Token '${csrfToken}'." }
    }

    fun execute(req: HttpUriRequest): String {
        logger.trace { "Executing ${req.method.toUpperCase()} request" }
        return this.httpClient.execute(req, this.httpContext).entity.content.bufferedReader().readText()
    }
}

