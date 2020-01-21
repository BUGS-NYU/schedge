package services

import com.github.kittinunf.fuel.Fuel
import models.Term
import mu.KotlinLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

private val queryLogger = KotlinLogging.logger("services.query_catalog")
private const val ROOT_URL = "https://m.albert.nyu.edu/app/catalog/classSearch"
/**
 * The meat of querying the catalog resides here.
 */
fun querySchool(term: Term): String {
    queryLogger.info { "querying catalog for term=$term"}
    val request = Fuel.get("${ROOT_URL}/${term.id}")
    return request.response().toString()
}

