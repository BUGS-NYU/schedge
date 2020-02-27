package services

// @CodeOrg This file should be in java/scraping

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

fun querySection(term: Term, registrationNumber: Int): String {
    return querySectionAsync(term, registrationNumber).get()
}

fun querySectionAsync(term: Term, registrationNumber: Int,
                      complete: (String) -> Unit): Future<Unit> {
    queryLogger.info { "Querying section in term=$term with registrationNumber=$registrationNumber..." }
    require(registrationNumber > 0) { "Registration numbers aren't negative!" }
    val future = CompletableFuture<Unit>()

    Fuel.get(DATA_URL + "${term.id}/${registrationNumber}").response { _, response, _ ->
        complete(String(response.data))
        future.complete(Unit)
    }

    return future
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

fun querySections(term: Term, registrationNumbers: List<Int>,
                  batchSizeNullable: Int?): Sequence<String> {
    if (registrationNumbers.size > 1) {
        queryLogger.info { "Querying section in term = $term" }
    }

    val batchSize = batchSizeNullable
            ?: max(5, min(registrationNumbers.size / 5, 20))

    return SimpleBatchedFutureEngine<Int, String>(
            registrationNumbers,
            batchSize
    ) { registrationNumber, _ ->
        querySectionAsync(term, registrationNumber)
    }.asSequence().filterNotNull()
}

