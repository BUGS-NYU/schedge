package services

import com.github.kittinunf.fuel.Fuel
import models.SubjectCode
import models.Term
import mu.KotlinLogging
import java.io.IOException
import java.net.HttpCookie
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import kotlin.math.max
import kotlin.math.min

private val queryLogger = KotlinLogging.logger("services.query_section")
private val DATA_URL = "https://m.albert.nyu.edu/app/catalog/classsection/NYUNV/"

fun querySection(term: Term, registrationNumber: Int): String =
    querySectionAsync(term, registrationNumber).get()

fun querySection(term: Term, registrationNumbers: List<Int>): Sequence<String> {
    if (registrationNumbers.size > 1)
      queryLogger.info { "Querying multiple sections..." }
    return batchRequest(
        registrationNumbers,
        max(5, min(registrationNumbers.size / 5, 20)) // @Performance What should this number be?
    ) { registrationNumber ->
        require(registrationNumber > 0) { "Registration numbers aren't negative!" }
        val future = CompletableFuture<String>()

        Fuel.get(DATA_URL + "${term.id}/${registrationNumber}").response { _, response, _ ->
            future.complete(String(response.data))
        }
        future
    }
}

private fun querySectionAsync(
    term: Term,
    registrationNumber: Int
): Future<String> {
  queryLogger.info { "Querying section in term=$term with registrationNumber=$registrationNumber..." }
  require(registrationNumber > 0) { "Registration numbers aren't negative!" }
  val future = CompletableFuture<String>()

  Fuel.get(DATA_URL + "${term.id}/${registrationNumber}").response { _, response, _ ->
    future.complete(String(response.data))
  }

  return future
}
