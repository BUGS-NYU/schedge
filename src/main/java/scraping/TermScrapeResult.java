package scraping;

import static utils.Nyu.*;

import java.util.*;
import java.util.function.*;

public interface TermScrapeResult extends Iterator<ArrayList<Course>> {
  ArrayList<School> getSchools();

  // @Note: This happens to allow JSON serialization of this object to
  // correctly run scraping, by forcing the serialization of the object to
  // run this method, which then consumes the iterator. It's stupid.
  //
  //                                  - Albert Liu, Nov 10, 2022 Thu 22:21
  default ArrayList<Course> getCourses() {
    var courses = new ArrayList<Course>();
    while (this.hasNext()) {
      courses.addAll(this.next());
    }

    return courses;
  }

  Term term();
}
