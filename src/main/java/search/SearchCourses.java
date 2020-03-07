package search;

import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchCourses {

    public static List<Integer> searchCourses(int epoch, String queryString, Integer totalNullable) {

        IndexSearcher searcher = GetResources.getSearcher(epoch);
        QueryParser qParser = new MultiFieldQueryParser(new String[]{"name", "description", "instructors", "subject", "school"}, GetResources.analyzer);
        Query query;
        try {
            query = qParser.parse(queryString);
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

        return Stream.of(hits).map(hit -> {
            try {
                return searcher.doc(hit.doc);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).map(doc -> Integer.parseInt(doc.get("id"))).collect(Collectors.toList());
    }
}
