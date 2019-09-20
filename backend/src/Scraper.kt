package schedge

// import java.nio.charset.StandardCharsets.UTF_8
// import org.apache.http.HttpEntity
// import java.io.BufferedReader
// import org.apache.http.client.ClientProtocolException
// import org.apache.http.client.ResponseHandler
// import org.apache.http.HttpResponse
// import org.apache.http.util.EntityUtils
// import org.apache.http.client.CookieStore
import schedge.Term
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

    /**
     * Send a post request for a specific term, school, and subject, getting back
     * all relevant data for that triple in XML format.
     */
    fun post_request_xml(term: Term, school: String, subject: String): String {
      val post_request = HttpPost(DATA_URL)

      val params = mutableListOf(
        KVPair("CSRFToken", csrf_token),
        KVPair("term", term.id.toString()),
        KVPair("acad_group", school.toString()),
        KVPair("subject", subject.toString())
      )
      post_request.setEntity(UrlEncodedFormEntity(params))

      val referrer_url = "${ROOT_URL}/${term.id}"
      post_request.addHeader("Referrer", referrer_url)

      val content = http_client.execute(post_request).getEntity().getContent()
      return content.bufferedReader().readText()
    }

}
