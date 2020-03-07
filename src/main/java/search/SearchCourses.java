package search;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

public class SearchCourses {

  public static List<Integer> searchCourses(int epoch, String queryString,
                                            Integer totalNullable) {

    IndexSearcher searcher = GetResources.getSearcher(epoch);
    TermQuery nameQuery = new TermQuery(new Term("name", queryString));
    TermQuery descQuery = new TermQuery(new Term("description", queryString));
    TermQuery instrQuery = new TermQuery(new Term("instructors", queryString));

    Query query =
        new BooleanQuery.Builder()
            .add(new BoostQuery(nameQuery, 2.0f), BooleanClause.Occur.SHOULD)
            .add(new BoostQuery(instrQuery, 1.1f), BooleanClause.Occur.SHOULD)
            .add(descQuery, BooleanClause.Occur.SHOULD)
            .build();

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
