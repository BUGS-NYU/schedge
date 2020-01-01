package services

import com.github.kittinunf.fuel.Fuel
import models.Term
import mu.KotlinLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import kotlin.math.max
import kotlin.math.min
import scraping.SimpleBatchedFutureEngine

private val queryLogger = KotlinLogging.logger("services.query_section")
private val DATA_URL = "https://m.albert.nyu.edu/app/catalog/classsection/NYUNV/"

fun querySection(term: Term, registrationNumber: Int): String =
    querySectionAsync(term, registrationNumber).get()

fun querySection(term: Term, registrationNumbers: List<Int>): Sequence<String> {
    if (registrationNumbers.size > 1)
      queryLogger.info { "Querying multiple sections..." }
    return SimpleBatchedFutureEngine(
        registrationNumbers, max(5, min(registrationNumbers.size / 5, 20))
    ) { registrationNumber, _ ->
        require(registrationNumber > 0) { "Registration numbers aren't negative!" }
        val future = CompletableFuture<String>()

        Fuel.get(DATA_URL + "${term.id}/${registrationNumber}").response { _, response, _ ->
            future.complete(String(response.data))
        }
        future
    }.asSequence()
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
