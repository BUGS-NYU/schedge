package scraping;

import java.util.NoSuchElementException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.concurrent.Future;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;

/**
 * This class handles batch resoltion of futures.
 *
 * The engine internally uses an Array to batch futures, filling the array with
 * futures then waiting on each one in turn as you call next.
 *
 * @author Albert Liu
 */
public class SimpleBatchedFutureEngine<Input, Output>
    implements Iterator<Output> {

  private static Logger logger;

  int pendingRequests;
  int currentMailbox = 0;
  ArrayList<Future<Output>> mailboxes;
  Iterator<Input> inputData;
  BiFunction<Input, Integer, Future<Output>> callback;

  /**
   * Creates a SimpleBatchFutureEngine. Calling this constructor fills the batch
   * with futures until either there are no more or until the batch is full.
   *
   * @param inputData The data to source the futures from.
   * @param batchSize The size of the batch.
   * @param callback The source of futures; takes in an element from inputDdata
   * and an index into the batch.
   */
  public SimpleBatchedFutureEngine(
      List<Input> inputData, int batchSize,
      BiFunction<Input, Integer, Future<Output>> callback) {
    this(inputData.iterator(), batchSize, callback);
  }

  /**
   * Creates a SimpleBatchFutureEngine. Calling this constructor fills the batch
   * with futures until either there are no more or until the batch is full.
   *
   * @param inputData The data to source the futures from.
   * @param batchSize The size of the batch.
   * @param callback The source of futures; takes in an element from inputDdata
   * and an index into the batch.
   */
  public SimpleBatchedFutureEngine(
      Iterator<Input> inputData, int batchSize,
      BiFunction<Input, Integer, Future<Output>> callback) {
    if (batchSize < 0)
      throw new IllegalArgumentException("batchSize must be positive!");

    this.mailboxes = new ArrayList<>();

    for (int count = 0; inputData.hasNext() && count < batchSize; count++)
      this.mailboxes.add(callback.apply(inputData.next(), count));

    this.pendingRequests = this.mailboxes.size();
    this.inputData = inputData;
    this.callback = callback;
  }

  private static <E> E getFuture(Future<E> future) throws ExecutionException {
    while (true) {
      try {
        return future.get();
      } catch (CancellationException e) {
        return null;
      } catch (InterruptedException e) {
      }
    }
  }

  public boolean hasNext() { return pendingRequests > 0; }

  @Override
  public Output next() {
    if (pendingRequests <= 0)
      throw new NoSuchElementException();

    Output fetchedResult = null;
    do {
      try {
        fetchedResult = getFuture(mailboxes.get(currentMailbox));
      } catch (ExecutionException e) {
        throw new RuntimeException(e);
      }

      if (inputData.hasNext()) {
        mailboxes.set(currentMailbox,
                      callback.apply(inputData.next(), currentMailbox));
      } else {
        pendingRequests--;
      }

      currentMailbox++;
      if (currentMailbox == mailboxes.size())
        currentMailbox = 0;
    } while (fetchedResult == null && pendingRequests > 0);

    return fetchedResult;
  }
}
