package schedge

import schedge.models.Term
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.message.BasicNameValuePair as KVPair
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.client.protocol.HttpClientContext


class Scraper {
    companion object {
        const val ROOT_URL = "https://m.albert.nyu.edu/app/catalog/classSearch"
        const val DATA_URL = "https://m.albert.nyu.edu/app/catalog/getClassSearch"
    }

    private val httpClient: CloseableHttpClient = HttpClients.custom().useSystemProperties().build()
    private val httpContext = HttpClientContext.create().apply {
        cookieStore = BasicCookieStore()
    }

    val csrfToken: String
        get() {
            val cookie = httpContext.cookieStore.cookies.find { cookie ->
                cookie.name == "CSRFCookie"
            }

            if (cookie == null)
                throw Exception("Couldn't get CSRF token from NYU.")
            else return cookie.value
        }

    init {
        // Get a CSRF token for this scraper. This token allows us to get data straight
        // from NYU's internal web APIs.
        val response = httpClient.execute(HttpGet(ROOT_URL), httpContext)
        response.close()

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

        return queryCourses(postRequest)
    }

    private fun queryCourses(query: HttpPost): String {
        val content = httpClient.execute(query, httpContext)!!.entity.content
        return content.bufferedReader().readText()
    }

    fun getCourseQuery(
            term: Term,
            school: String,
            subject: String,
            catalogNumber: Int?,
            keyword: String?,
            classNumber: Int?,
            location: String?
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
            it.addHeader("Referrer", "${ROOT_URL}/${term.id}")
            it.addHeader("Host", "m.albert.nyu.edu")
        }

    }
}
