package scraping;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
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

  static long DEFAULT_TIMEOUT = 10;

  private int pendingRequests;
  private long timeout;
  private ArrayList<Future<Output>> mailboxes;
  private Iterator<Input> inputData;
  private BiFunction<Input, Integer, Future<Output>> callback;

  public SimpleBatchedFutureEngine(
      List<Input> inputData, int batchSize,
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
      List<Input> inputData, int batchSize, long timeout,
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

    this.mailboxes = new ArrayList<>();

    for (int count = 0; inputData.hasNext() && count < batchSize; count++)
      this.mailboxes.add(callback.apply(inputData.next(), count));

    this.pendingRequests = this.mailboxes.size();
    this.inputData = inputData;
    this.callback = callback;
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

  public Output checkMailboxes() {
    for (int i = 0; i < pendingRequests; i++) {
      Future<Output> future = mailboxes.get(i);
      if (future.isDone()) {

        Output value = getFuture(future);
        if (inputData.hasNext()) {
          mailboxes.set(i, callback.apply(inputData.next(), i));
        } else {
          pendingRequests--;
          mailboxes.set(i, mailboxes.get(pendingRequests));
          mailboxes.remove(pendingRequests);
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

    Output fetchedResult = null;
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

    return fetchedResult;
  }
}
