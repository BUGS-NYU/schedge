package scraping;

import static utils.Nyu.*;

import java.util.*;
import java.util.function.*;
import utils.Try;

public abstract class TermScrapeResult implements Iterator<ArrayList<Course>> {
  protected final Consumer<ScrapeEvent> consumer;
  protected final Try ctx;
  protected final Term term;

  public TermScrapeResult(Term term, Consumer<ScrapeEvent> consumer, Try ctx) {
    consumer = Objects.requireNonNullElse(consumer, e -> {});

    this.ctx = ctx;
    this.term = term;
    this.consumer = consumer;
  }

  public abstract ArrayList<School> getSchools();

  // @Note: This happens to allow JSON serialization of this object to
  // correctly run scraping, by forcing the serialization of the object to
  // run this method, which then consumes the iterator. It's stupid.
  //
  //                                  - Albert Liu, Nov 10, 2022 Thu 22:21
  public final ArrayList<Course> getCourses() {
    var courses = new ArrayList<Course>();
    while (this.hasNext()) {
      courses.addAll(this.next());
    }

    return courses;
  }
}
