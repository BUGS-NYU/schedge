package scraping;

import static utils.Nyu.*;

import java.util.*;

public record TermScrapeResult(
    Term term, ArrayList<School> schools, Iterable<List<Course>> courses) {}
