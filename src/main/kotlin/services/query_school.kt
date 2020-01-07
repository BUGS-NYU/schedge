package services

import com.github.kittinunf.fuel.Fuel
import models.Term
import mu.KotlinLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

private val queryLogger = KotlinLogging.logger("services.query_catalog")
private const val ROOT_URL = "https://m.albert.nyu.edu/app/catalog/classSearch/1204"
private const val DATA_URL = "https://m.albert.nyu.edu/app/catalog/getClassSearch"

/**
 * Query the catalog for data
 */
//fun queryCatalog(term: Term): String {
//    return queryCatalog(term, subjectCode, getContext()).get()
//            ?: throw IOException("No classes found matching criteria school=${subjectCode.school}, subject=${subjectCode.abbrev}")
//}

//fun queryCatalog(term: Term,): String {
//    if (subjectCodes.size > 1) {
//        queryLogger.info { "querying catalog for term=$term with multiple subjects..." }
//    }
//
//    val batchSize = batchSizeNullable ?: max(5, min(subjectCodes.size / 5, 20)) // @Performance What should this number be?
//    val contexts = Array(batchSize) { getContextAsync() }.map { it.get() }.toTypedArray()
//
//    return SimpleBatchedFutureEngine(subjectCodes, batchSize) { subjectCode, idx ->
//        queryCatalog(term, subjectCode, contexts[idx])
//    }.asSequence().filterNotNull()
//}

/**
 * The meat of querying the catalog resides here.
 */
fun querySchool(term: Term): String {
    queryLogger.info { "querying catalog for term=$term"}

    val request = Fuel.get(ROOT_URL)
    return request.response().toString()
}
//
///**
// * Get a CSRF Token from NYU
// */
//private fun getContext(): HttpContext = getContextAsync().get()
//
//private fun getContextAsync(): Future<HttpContext> {
//    val future = CompletableFuture<HttpContext>()
//
//    queryLogger.info { "Getting CSRF token..." }
//    Fuel.get(ROOT_URL).response { _, response, _ ->
//        val cookies = response.headers["Set-Cookie"].flatMap {
//            HttpCookie.parse(it)!!
//        }
//        val token = cookies.find {
//            it.name == "CSRFCookie"
//        }?.value
//
//        if (token == null) {
//            queryLogger.error("Couldn't find cookie with name=CSRFCookie")
//            throw IOException("NYU servers did something unexpected.")
//        }
//
//        queryLogger.info { "Retrieved CSRF token `${token}`" }
//        future.complete(HttpContext(token, cookies))
//    }
//    return future
//}
//
//private data class HttpContext(val csrfToken: String, val cookies: List<HttpCookie>)
//
