package services

// @CodeOrg This file should be in java/scraping

import com.github.kittinunf.fuel.Fuel
import models.SubjectCode
import models.Term
import mu.KotlinLogging
import scraping.SimpleBatchedFutureEngine
import scraping.models.CatalogQueryData
import scraping.models.Course
import scraping.models.Section
import scraping.models.SectionAttribute
import java.io.IOException
import java.net.HttpCookie
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import kotlin.math.max
import kotlin.math.min
import kotlin.streams.toList

private val queryLogger = KotlinLogging.logger("services.query_catalog")
private const val ROOT_URL = "https://m.albert.nyu.edu/app/catalog/classSearch"
private const val DATA_URL = "https://m.albert.nyu.edu/app/catalog/getClassSearch"

/**
 * Query the catalog for data
 */
fun queryCatalog(term: Term, subjectCode: SubjectCode): CatalogQueryData {
    return queryCatalog(term, subjectCode, getContext()).get()
            ?: throw IOException("No classes found matching criteria school=${subjectCode.school}, subject=${subjectCode.abbrev}")
}


fun queryCatalog(term: Term, subjectCodes: List<SubjectCode>,
                 batchSizeNullable: Int? = null): Sequence<CatalogQueryData> {
    if (subjectCodes.size > 1) {
        queryLogger.info { "querying catalog for term=$term with multiple subjects..." }
    }

    val batchSize = batchSizeNullable
            ?: max(5, min(subjectCodes.size / 5, 20)) // @Performance What should this number be?
    val contexts = Array(batchSize) { getContextAsync() }.map { it.get() }.toTypedArray()

    return SimpleBatchedFutureEngine<SubjectCode, CatalogQueryData>(
            subjectCodes,
            batchSize
    ) { subjectCode, idx ->
        queryCatalog(term, subjectCode, contexts[idx])
    }.asSequence().filterNotNull()
}

/**
 * The meat of querying the catalog resides here.
 */
private fun queryCatalog(term: Term, subjectCode: SubjectCode,
                         httpContext: HttpContext): Future<CatalogQueryData?> {
    queryLogger.info { "querying catalog for term=$term and subject=$subjectCode..." }


    val future = CompletableFuture<CatalogQueryData?>()

    val request = Fuel.post(DATA_URL).apply {
        set("Referrer", "${ROOT_URL}/${term.id}")
        set("Host", "m.albert.nyu.edu")
        set("Accept-Language", "en-US,en;q=0.5")
        set("Accept-Encoding", "gzip, deflate, br")
        set("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
        set("X-Requested-With", "XMLHttpRequest")
        set("Content-Length", "129")
        set("Origin", "https://m.albert.nyu.edu")
        set("DNT", "1")
        set("Connection", "keep-alive")
        set("Referer", "https://m.albert.nyu.edu/app/catalog/classSearch")
        set("Cookie", httpContext.cookies.joinToString(";") { it.toString() })

        val params = listOf( // URL params
                "CSRFToken" to httpContext.csrfToken,
                "term" to term.id.toString(),
                "acad_group" to subjectCode.school,
                "subject" to subjectCode.abbrev
        )
        queryLogger.debug { "Params are ${params}." }
        val bodyValue = params.joinToString("&") { it.first + '=' + it.second }
        body(bodyValue)
    }

    request.response { _, response, _ ->
        val result = String(response.data)
        if (result == "No classes found matching your criteria.") {
            queryLogger.warn { "No classes found matching criteria school=${subjectCode.school}, subject=${subjectCode.abbrev}" }
            future.complete(null);
        } else {
            future.complete(CatalogQueryData(subjectCode, result))
        }
    }
    return future
}

/**
 * Get a CSRF Token from NYU
 */
private fun getContext(): HttpContext = getContextAsync().get()

private fun getContextAsync(): Future<HttpContext> {
    val future = CompletableFuture<HttpContext>()

    queryLogger.info { "Getting CSRF token..." }
    Fuel.get(ROOT_URL).response { _, response, _ ->
        val cookies = response.headers["Set-Cookie"].flatMap {
            HttpCookie.parse(it)!!
        }
        val token = cookies.find {
            it.name == "CSRFCookie"
        }?.value

        if (token == null) {
            queryLogger.error("Couldn't find cookie with name=CSRFCookie")
            throw IOException("NYU servers did something unexpected.")
        }

        queryLogger.info { "Retrieved CSRF token `${token}`" }
        future.complete(HttpContext(token, cookies))
    }
    return future
}

private data class HttpContext(val csrfToken: String, val cookies: List<HttpCookie>)

