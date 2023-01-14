package scraping;

import static utils.Nyu.*;

import java.util.*;
import java.util.function.*;

public interface TermScrapeResult extends Iterator<List<Course>> {
  ArrayList<School> schools();

  // @Note: This happens to allow JSON serialization of this object to
  // correctly run scraping, by forcing the serialization of the object to
  // run this method, which then consumes the iterator. It's stupid.
  //
  //                                  - Albert Liu, Nov 10, 2022 Thu 22:21
  default List<Course> getCourses() {
    var courses = new ArrayList<Course>();
    while (this.hasNext()) {
      courses.addAll(this.next());
    }

    return courses;
  }

  Term term();

  final class Impl implements TermScrapeResult {
    Term term;
    ArrayList<School> schools;
    Iterator<List<Course>> iterator;

    public Impl(Term term, ArrayList<School> schools, Iterable<List<Course>> iterable) {
      this.term = term;
      this.schools = schools;
      this.iterator = iterable.iterator();
    }

    public Term term() {
      return term;
    }

    public ArrayList<School> schools() {
      return schools;
    }

    public boolean hasNext() {
      return this.iterator.hasNext();
    }

    public List<Course> next() {
      return this.iterator.next();
    }
  }
}
