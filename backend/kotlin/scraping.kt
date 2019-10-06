import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair as KVPair
import models.Term
import java.io.IOException

class Scraper {
    companion object {
        const val ROOT_URL = "https://m.albert.nyu.edu/app/catalog/classSearch"
        const val DATA_URL = "https://m.albert.nyu.edu/app/catalog/getClassSearch"
        const val CATALOG_URL = "https://m.albert.nyu.edu/app/catalog/classsection/NYUNV"
    }

    private val logger = Logging.getLogger(Logging.WARN)
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
                logger.info {
                    val cookies = httpContext.cookieStore.cookies.map {
                        "${it.name}: \"${it.value}\""
                    }
                    "Couldn't find `CSRFCookie`. Cookies found were [\n  ${cookies.joinToString(",\n  ")}]."
                }

                logger.error(
                    "NYU servers did something unexpected.",
                    ::IOException
                )
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

    /**
     * Send a post request for a specific term, school, and subject, getting back
     * all relevant data for that triple in XML format.
     *
     * TODO Return null when we get a bad response
     *
     */
    fun queryCourses(
        term: Term,
        school: String,
        subject: String,
        catalogNumber: Int? = null,
        keyword: String? = null,
        classNumber: Int? = null,
        location: String? = null
    ): String {
        val postRequest = getCourseQuery(
                term = term,
                school = school,
                subject = subject,
                catalogNumber = catalogNumber,
                keyword = keyword,
                classNumber = classNumber,
                location = location
        )
        val content = httpClient.execute(postRequest, httpContext).entity.content
        return content.bufferedReader().readText()
    }

    fun querySection(term: Term, registrationNumber: Long): String {
        val query = HttpGet("$CATALOG_URL/${term.id}/${registrationNumber}")
        val content = httpClient.execute(query, httpContext).entity.content
        return content.bufferedReader().readText()
    }


    fun getCourseQuery(
        term: Term,
        school: String,
        subject: String,
        catalogNumber: Int? = null,
        keyword: String? = null,
        classNumber: Int? = null,
        location: String? = null
    ): HttpPost {
        val params = mutableListOf( // URL params
                KVPair("CSRFToken", csrfToken),
                KVPair("term", term.id.toString()),
                KVPair("acad_group", school),
                KVPair("subject", subject),
                KVPair("catalog_nbr", catalogNumber?.toString() ?: ""),
                KVPair("keyword", keyword ?: ""),
                KVPair("class_nbr", classNumber?.toString() ?: ""),
                KVPair("nyu_location", location ?: "")
        )

        return HttpPost(DATA_URL).also {
            it.entity = UrlEncodedFormEntity(params)
            it.addHeader("Referrer", "$ROOT_URL/${term.id}")
            it.addHeader("Host", "m.albert.nyu.edu")
        }

    }
}
