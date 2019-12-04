package services

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import kotlin.math.min

fun <Input, Output> batchRequest(
    input: List<Input>,
    batchSize: Int,
    callback: (Input) -> Future<Output?>
): Sequence<Output> = BatchRequestEngine(input, batchSize, {
    val future = CompletableFuture<Unit>();
    future.complete(Unit);
    future
}, { it, _ -> callback(it) }).asSequence()


fun <Input, Context, Output> batchRequest(
    input: List<Input>,
    batchSize: Int,
    initContext: (Int) -> Future<Context>,
    callback: (Input, Context) -> Future<Output?>
): Sequence<Output> = BatchRequestEngine(input, batchSize, initContext, callback).asSequence()

/**
 * This class tries to emulate batch processing, with multiple requests potentially in flight at the same time.
 *
 * Internally, it maintains a set of mailboxes which it iterates over,
 * waiting on each one for new data to arrive.
 * While it waits, other "mail" might be arriving in other mailboxes.
 */
private class BatchRequestEngine<InputData, Context, Output>
constructor(
    val inputData: List<InputData>,
    batchSize: Int,
    initializeContext: (Int) -> Future<Context>,
    val callback: (InputData, Context) -> Future<Output?> // @TODO Change this to give an integer index instead of giving a context
) :
    Iterator<Output> {

    init {
        require(batchSize > 0) { "Need to have a non-empty array size!" }
    }

    var inputIndex = min(inputData.size, batchSize)
    var pendingRequests = inputIndex
    var currentMailbox = 0
    val contexts = Array(inputIndex) { initializeContext(it) }.map { it.get() }
    val mailboxes = Array(inputIndex) {
        callback(inputData[it], contexts[it])
    }
    var currentResult = tryGetNext()

    private fun tryGetNext(): Output? {
        var fetchedResult: Output? = null
        while (fetchedResult == null && pendingRequests > 0) {
            fetchedResult = mailboxes[currentMailbox].get()
            if (inputIndex < inputData.size) {
                mailboxes[currentMailbox] = callback(inputData[inputIndex], contexts[currentMailbox])
                inputIndex++
            } else {
                pendingRequests--
            }
            currentMailbox++
            if (currentMailbox == contexts.size) currentMailbox = 0
        }
        return fetchedResult
    }

    override fun hasNext(): Boolean = currentResult != null

    override fun next(): Output {
        if (currentResult == null) throw NoSuchElementException()
        val cachedResult = this.currentResult
        this.currentResult = tryGetNext()
        return cachedResult!!
    }
}

