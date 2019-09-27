package schedge

import schedge.models.Term
import java.io.IOException
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.message.BasicNameValuePair as KVPair
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.client.protocol.HttpClientContext


public class Scraper {
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
     *
     * TODO Return null when we get a bad response
     *
     */
    public fun postRequestXml(term: Term, school: String, subject: String): String {

      val params = mutableListOf( // URL params
        KVPair("CSRFToken", csrf_token),
        KVPair("term", term.id.toString()),
        KVPair("acad_group", school.toString()),
        KVPair("subject", subject.toString())
      )

      val post_request = HttpPost(DATA_URL).also {
        it.setEntity(UrlEncodedFormEntity(params))
        it.addHeader("Referrer", "${ROOT_URL}/${term.id}")
      }

      val content = http_client.execute(post_request)!!.getEntity().getContent()
      return content.bufferedReader().readText()
    }
}
