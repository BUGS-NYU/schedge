package schedge

// import java.nio.charset.StandardCharsets.UTF_8
// import org.apache.http.HttpEntity
// import java.io.BufferedReader
// import org.apache.http.client.ClientProtocolException
// import org.apache.http.client.ResponseHandler
// import org.apache.http.HttpResponse
// import org.apache.http.util.EntityUtils
// import org.apache.http.client.CookieStore
import org.jsoup.Jsoup
import schedge.Term
import java.io.IOException
import org.jsoup.nodes.Element
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.message.BasicNameValuePair as KVPair
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.client.protocol.HttpClientContext;

class Scraper {
    companion object {
        val ROOT_URL = "https://m.albert.nyu.edu/app/catalog/classSearch/"
        val DATA_URL = "https://m.albert.nyu.edu/app/catalog/getClassSearch"
    }

    val http_client : CloseableHttpClient
    val csrf_token : String

    init {
      // Get a CSRF token for this scraper. This token allows us to get data straight
      // from NYU's internal web APIs.
      val http_context = HttpClientContext.create()
      http_context.setCookieStore(BasicCookieStore())

      http_client = HttpClients.createDefault()

      val get_request = HttpGet(ROOT_URL)
      val response = http_client.execute(get_request, http_context)
      response.close()

      val cookie = http_context.getCookieStore().cookies.find() {
        cookie -> cookie.name == "CSRFCookie"
      }

      if (cookie == null)
        throw Exception("Couldn't get CSRF token from NYU.")
      else csrf_token = cookie.value
    }

    fun term_data(term: Term, school: String, subject: String): String {
      val xml = Jsoup.parse(post_request_xml(term, school, subject))
      if (xml == null) {
        throw IOException("Jsoup.parse returned null")
      }

      val elements = xml.select("div.primary-head ~ *") // Get all siblings of the primary head
      val block_indices = elements.withIndex().filter {
        (_, element) -> element.tagName() == "div"
      }.map {
        (idx, _) -> idx
      }.toMutableList().also {
        it.add(elements.size)
      }

      val blocks = block_indices.windowed(2).map {
        (from, to) -> CourseListingNode(elements.get(from), elements.subList(from + 1, to))
      }

      val boi = blocks.map(::parse)

      return boi.toString()
    }

    /**
     * Send a post request for a specific term, school, and subject, getting back
     * all relevant data for that triple in XML format.
     */
    fun post_request_xml(term: Term, school: String, subject: String): String {

      val params = mutableListOf(
        KVPair("CSRFToken", csrf_token),
        KVPair("term", term.id.toString()),
        KVPair("acad_group", school.toString()),
        KVPair("subject", subject.toString())
      )

      val post_request = HttpPost(DATA_URL).also {
        it.setEntity(UrlEncodedFormEntity(params))
        it.addHeader("Referrer", "${ROOT_URL}/${term.id}")
      }

      val content = http_client.execute(post_request).getEntity().getContent()
      return content.bufferedReader().readText()
    }

}

data class CourseListingNode(val heading: Element, val sections: List<Element>)

fun parse(node: CourseListingNode): String {
  return ""
}
