package search;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchCourses {
  private static QueryParser nameQueryParser =
      new QueryParser("name", GetResources.analyzer);
  private static QueryParser descrQueryParser =
      new QueryParser("description", GetResources.analyzer);
  private static QueryParser instrQueryParser =
      new QueryParser("instructors", GetResources.analyzer);

  public static List<Integer> searchCourses(int epoch, String queryString,
                                            Integer totalNullable) {

    System.err.println(queryString);

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
      throw new RuntimeException(e);
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
