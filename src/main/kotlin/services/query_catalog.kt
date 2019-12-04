package services

import com.github.kittinunf.fuel.Fuel
import models.SubjectCode
import models.Term
import mu.KLogger
import mu.KotlinLogging
import java.io.IOException
import java.net.HttpCookie
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import kotlin.math.max
import kotlin.math.min

val queryLogger = KotlinLogging.logger("services.query_catalog");
private const val ROOT_URL = "https://m.albert.nyu.edu/app/catalog/classSearch"
private const val DATA_URL = "https://m.albert.nyu.edu/app/catalog/getClassSearch"

/**
 * Query the catalog for data
 */
fun queryCatalog(logger: KLogger, term: Term, subjectCode: SubjectCode): String {
    return queryCatalog(term, subjectCode, getToken()).get()
        ?: throw IOException("No classes found matching criteria school=${subjectCode.school}, subject=${subjectCode.abbrev}")
}

/**
 * The meat of querying the catalog resides here.
 */
private fun queryCatalog(term: Term, subjectCode: SubjectCode, csrfToken: String): Future<String?> {
    queryLogger.info { "querying catalog for term=$term and subject=$subjectCode..." }
    val params = listOf( // URL params
        "CSRFToken" to csrfToken,
        "term" to term.id.toString(),
        "acad_group" to subjectCode.school,
        "subject" to subjectCode.abbrev
    )

    queryLogger.debug { "Params are ${params}." }
    val future = CompletableFuture<String?>()

    Fuel.post(DATA_URL, params).set("Referrer", "${ROOT_URL}/${term.id}").set(
        "Host",
        "m.albert.nyu.edu"
    ).response { _, response, _ ->
        val result = String(response.data)
        if (result == "No classes found matching your criteria.") {
            queryLogger.warn { "No classes found matching criteria school=${subjectCode.school}, subject=${subjectCode.abbrev}" }
            future.complete(null);
        } else {
            future.complete(result)
        }
    }
    return future
}

/**
Querying Catalog given list of subject codes
@param logger
@param Term (class)
@param subjectCodes (list of subjectcode)
@return Sequence of String
 */
fun queryCatalog(logger: KLogger, term: Term, subjectCodes: List<SubjectCode>): Sequence<String> {
    if (subjectCodes.size > 1) {
        queryLogger.info { "querying catalog for term=$term with multiple subjects..." }
    }
    return QueryResults(term, subjectCodes, max(5, min(subjectCodes.size / 5, 20))).asSequence()
}

/**
 * This class tries to emulate batch processing, with multiple requests potentially in flight at the same time.
 *
 * Internally, it maintains a set of mailboxes which it iterates over, waiting on each one for new data to arrive.
 * While it waits, other "mail" might be arriving in other mailboxes.
 */
private class QueryResults(val term: Term, val subjects: List<SubjectCode>, arraySize: Int) :
    Iterator<String> {

    init {
        require(arraySize > 0) { "Need to have a non-empty array size!" }
        queryLogger.info { "Array size is $arraySize" }
    }

    var subjectsIndex = min(subjects.size, arraySize)
    var pendingRequests = subjectsIndex
    var arrayIndex = 0
    val tokens = Array(subjectsIndex) {
        getTokenAsync()
    }.map {
        it.get()
    }
    val requests = Array(subjectsIndex) {
        queryCatalog(term, subjects[it], tokens[it])
    }
    var result: String? = null

    init {
        queryLogger.info { "subjectsIndex is $subjectsIndex" }
        result = tryGetNext()
    }

    private fun tryGetNext(): String? {
        var fetchedResult: String? = null
        while (fetchedResult == null && pendingRequests > 0) {
            fetchedResult = requests[arrayIndex].get()
            queryLogger.info { "fetchedResult is $fetchedResult" }
            if (subjectsIndex < subjects.size) {
                requests[arrayIndex] = queryCatalog(term, subjects[subjectsIndex], tokens[arrayIndex])
                subjectsIndex++
            } else {
                pendingRequests--
            }
            arrayIndex++
            if (arrayIndex == tokens.size) arrayIndex = 0
        }

        return fetchedResult
    }

    override fun hasNext(): Boolean {
        return result != null
    }

    override fun next(): String {
        if (result == null) throw NoSuchElementException()
        val cachedResult = this.result
        this.result = tryGetNext()
        return cachedResult!!
    }

}

/**
 * Get a CSRF Token from NYU
 */
fun getToken(): String = getTokenAsync().get()

fun getTokenAsync(): Future<String> {
    val future = CompletableFuture<String>()

    queryLogger.info { "Getting CSRF token..." }
    Fuel.get(ROOT_URL).response { _, response, _ ->
        val token = response.headers["Set-Cookie"].flatMap {
            HttpCookie.parse(it)!!
        }.find {
            it.name == "CSRFCookie"
        }?.value

        if (token == null) {
            queryLogger.error("Couldn't find cookie with name=CSRFCookie")
            throw IOException("NYU servers did something unexpected.")
        }

        queryLogger.info { "Retrieved CSRF token `${token}`" }
        future.complete(token)
    }
    return future
}

