package services

import models.SubjectCode
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
/**
Make Query Service
 */
fun queryCatalog(logger: KLogger, term: Term, subjectCode: SubjectCode): String = queryCatalog(logger, term, subjectCode, AlbertClient())
/**
Querying the catalog given one subject code.
@param Logger
@param Term (class)
@param SubjectCode (class)
@param AlbertClient (class)
@return String
 */
private fun queryCatalog(logger: KLogger, term: Term, subjectCode: SubjectCode, client: AlbertClient): String {
    logger.info { "querying catalog for term=$term and subject=$subjectCode..." }
    val params = mutableListOf( // URL params
        BasicNameValuePair("CSRFToken", client.csrfToken),
        BasicNameValuePair("term", term.id.toString()),
        BasicNameValuePair("acad_group", subjectCode.school),
        BasicNameValuePair("subject", subjectCode.abbrev)
    )
    logger.debug { "Params are ${params}." }

    val request = HttpPost(DATA_URL).apply {
        entity = UrlEncodedFormEntity(params)
        addHeader("Referrer", "${ROOT_URL}/${term.id}")
        addHeader("Host", "m.albert.nyu.edu")
    }

    val result = client.execute(request)
    return if (result == "No classes found matching your criteria.") {
        throw IOException("No classes found matching criteria school=${subjectCode.school}, subject=${subjectCode.abbrev}")
    } else {
        result
    }
}

/**
Querying Catalog given list of subject codes
@param logger
@param Term (class)
@param subjectCodes (list of subjectcode)
@return Sequence of String
 */
fun queryCatalog(logger: KLogger, term: Term, subjectCodes: List<SubjectCode>): Sequence<String> {
    if (subjectCodes.size > 1) {
        logger.info { "querying catalog for term=$term with multiple subjects..." }
    }
    val client = AlbertClient()

    return subjectCodes.asSequence().map { subject ->
        queryCatalog(logger, term, subject, client)
    }
}

private const val ROOT_URL = "https://m.albert.nyu.edu/app/catalog/classSearch"
private const val DATA_URL = "https://m.albert.nyu.edu/app/catalog/getClassSearch"
/**
Albert Client class to handle HTTP Request
 */
private class AlbertClient {
    private val logger = KotlinLogging.logger {}
    private val httpClient = HttpClients.custom().useSystemProperties().build()
    private val httpContext = HttpClientContext.create().apply {
        cookieStore = BasicCookieStore()
    }
    /**
     * Retrieve the csrfToken for making HTTP request to Albert Mobile
     */
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
    /**
    Making the HTTPRequest to Albert Mobile
    @param HTTPUriRequest
    @return String
     */
    fun execute(req: HttpUriRequest): String {
        logger.trace { "Executing ${req.method.toUpperCase()} request" }
        return this.httpClient.execute(req, this.httpContext).entity.content.bufferedReader().readText()
    }
}

