package scraping;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiFunction;

/**
 * This class handles batch resoltion of futures.
 *
 * The engine internally uses an Array to batch futures, filling the array with
 * futures then waiting on each one in turn as you call next.
 *
 * @author Albert Liu
 */
public class SimpleBatchedFutureEngine<Input, Output>
    implements Iterator<Output>, Iterable<Output> {

  static long DEFAULT_TIMEOUT = 10;

  private int pendingRequests;
  private long timeout;
  private boolean iteratorHasNext;
  private Object[] mailboxes;
  private Iterator<Input> inputData;
  private BiFunction<Input, Integer, Future<Output>> callback;

  public SimpleBatchedFutureEngine(
      Iterable<Input> inputData, int batchSize,
      BiFunction<Input, Integer, Future<Output>> callback) {
    this(inputData.iterator(), batchSize, callback);
  }

  public SimpleBatchedFutureEngine(
      Iterator<Input> inputData, int batchSize,
      BiFunction<Input, Integer, Future<Output>> callback) {
    this(inputData, batchSize, DEFAULT_TIMEOUT, callback);
  }

  /**
   * Creates a SimpleBatchFutureEngine. Calling this constructor fills the batch
   * with futures until either there are no more or until the batch is full.
   *
   * @param inputData The data to source the futures from.
   * @param batchSize The size of the batch.
   * @param timeout The amount of time to wait before checking for completed
   * tasks again.
   * @param callback The source of futures; takes in an element from inputDdata
   * and an index into the batch.
   */
  public SimpleBatchedFutureEngine(
      Iterable<Input> inputData, int batchSize, long timeout,
      BiFunction<Input, Integer, Future<Output>> callback) {
    this(inputData.iterator(), batchSize, timeout, callback);
  }

  /**
   * Creates a SimpleBatchFutureEngine. Calling this constructor fills the batch
   * with futures until either there are no more or until the batch is full.
   *
   * @param inputData The data to source the futures from.
   * @param batchSize The size of the batch.
   * @param timeout The amount of time to wait before checking for completed
   * tasks again.
   * @param callback The source of futures; takes in an element from inputDdata
   * and an index into the batch.
   */
  public SimpleBatchedFutureEngine(
      Iterator<Input> inputData, int batchSize, long timeout,
      BiFunction<Input, Integer, Future<Output>> callback) {
    if (batchSize < 0)
      throw new IllegalArgumentException("batchSize must be positive!");

    this.mailboxes = new Object[batchSize];

    for (pendingRequests = 0;
         (iteratorHasNext = inputData.hasNext()) && pendingRequests < batchSize;
         pendingRequests++) {
      mailboxes[pendingRequests] =
          callback.apply(inputData.next(), pendingRequests);
    }
    this.inputData = inputData;
    this.callback = callback;
    this.timeout = timeout;
  }

  private static <E> E getFuture(Future<E> future) {
    while (true) {
      try {
        return future.get();
      } catch (CancellationException e) {
        return null;
      } catch (ExecutionException e) {
        throw new RuntimeException(e);
      } catch (InterruptedException e) {
      }
    }
  }

  public boolean hasNext() { return pendingRequests > 0; }

  // @TODO Remove null checks from all of the methods in this class
  public Output checkMailboxes() {
    for (int i = 0; i < pendingRequests; i++) {
      @SuppressWarnings("unchecked")
      Future<Output> future = (Future<Output>)mailboxes[i];
      if (future.isDone()) {

        Output value = getFuture(future);
        if (iteratorHasNext && (iteratorHasNext = inputData.hasNext())) {
          mailboxes[i] = callback.apply(inputData.next(), i);
        } else {
          pendingRequests--;
          mailboxes[i] = mailboxes[pendingRequests];
          mailboxes[pendingRequests] = null;
        }

        if (value != null)
          return value;
      }
    }
    return null;
  }

  @Override
  public Output next() {
    if (pendingRequests <= 0)
      throw new NoSuchElementException();

    Output fetchedResult;
    while (pendingRequests > 0) {
      fetchedResult = checkMailboxes();
      if (fetchedResult != null) {
        return fetchedResult;
      } else {
        try {
          Thread.sleep(timeout);
        } catch (InterruptedException e) {
        }
      }
    }

    return null;
  }

  @NotNull
  @Override
  public Iterator<Output> iterator() {
    return this;
  }
}
