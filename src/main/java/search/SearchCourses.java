package search;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchCourses {
  private static QueryParser nameQueryParser =
      new QueryParser("name", GetResources.analyzer);
  private static QueryParser descrQueryParser =
      new QueryParser("description", GetResources.analyzer);
  private static QueryParser instrQueryParser =
      new QueryParser("instructors", GetResources.analyzer);

  private static final Logger logger =
      LoggerFactory.getLogger("search.SearchCourses");

  public static List<Integer> searchCourses(int epoch, String queryString,
                                            Integer totalNullable) {
    IndexSearcher searcher = GetResources.getSearcher(epoch);
    Query query = null;
    try {
      query =
          new BooleanQuery.Builder()
              .add(new BoostQuery(nameQueryParser.parse(queryString), 2.0f),
                   BooleanClause.Occur.SHOULD)
              .add(new BoostQuery(instrQueryParser.parse(queryString), 1.1f),
                   BooleanClause.Occur.SHOULD)
              .add(descrQueryParser.parse(queryString),
                   BooleanClause.Occur.SHOULD)
              .build();
    } catch (ParseException e) {
      logger.warn("Parsing error for query string: " + queryString);
      logger.warn(e.getMessage());
    } catch (RuntimeException r) {
      logger.warn("Parsing error for query string: " + queryString);
      throw r;
    }

    ScoreDoc[] hits;
    int total = totalNullable != null ? totalNullable : 100;
    try {
      hits = searcher.search(query, total).scoreDocs;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return Stream.of(hits)
        .map(hit -> {
          try {
            return searcher.doc(hit.doc);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })
        .map(doc -> Integer.parseInt(doc.get("id")))
        .collect(Collectors.toList());
  }
}
