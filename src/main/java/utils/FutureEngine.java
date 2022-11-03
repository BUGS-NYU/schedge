package utils;

import static utils.Try.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

/**
 * This class handles batch resoltion of futures.
 *
 * @author Albert Liu
 */
public final class FutureEngine<Output>
    implements Iterator<Output>, Iterable<Output> {

  static final long DEFAULT_TIMEOUT = 10;

  private final ConcurrentLinkedQueue<Future<Output>> tasks;
  private final AtomicInteger size;
  private final long timeout;

  public FutureEngine() { this(DEFAULT_TIMEOUT); }

  public FutureEngine(long timeout) {
    this.tasks = new ConcurrentLinkedQueue<>();
    this.size = new AtomicInteger(0);
    this.timeout = timeout;
  }

  public void add(Future<Output> task) {
    this.tasks.add(task);
    this.size.incrementAndGet();
  }

  @Override
  public boolean hasNext() {
    return this.size.get() > 0;
  }

  @Override
  public Output next() {
    if (this.size.get() <= 0)
      throw new NoSuchElementException();

    // Just busy-wait for a task
    while (true) {
      Future<Output> box = this.tasks.poll();

      if (box.isDone()) {
        Output output = tcPass(() -> box.get());
        this.size.decrementAndGet();

        return output;
      }

      this.tasks.add(box);

      tcIgnore(() -> Thread.sleep(this.timeout));
    }
  }

  @Override
  public Iterator<Output> iterator() {
    return this;
  }
}
