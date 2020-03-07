package search;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import picocli.CommandLine;

public class GetResources {

  public final static Analyzer analyzer = new StandardAnalyzer();
  public static IndexWriterConfig config = new IndexWriterConfig(analyzer);
  // @Performance We need to eventually be able to delete indices, using AtomicInteger to be thread safe
  private static HashMap<Integer,SearchContext> contexts = new HashMap<>();

  private static SearchContext getSearchContext(int epoch) {
      synchronized (contexts) {
          if (contexts.containsKey(epoch)) {
              return contexts.get(epoch);
          } else {
              SearchContext context =  new SearchContext(epoch);
              contexts.put(epoch,context);
              return context;

          }
      }
  }

  public static IndexWriter getWriter(int epoch) {
      try {
          return new IndexWriter(getSearchContext(epoch).index, config);
      } catch (IOException e) {
          throw new RuntimeException(e);
      }
  }

  public static IndexSearcher getSearcher(int epoch) {
      return getSearchContext(epoch).getSearcher();
  }

  private static class SearchContext implements AutoCloseable {
      private final Directory index;
      private IndexSearcher searcher = null;

      SearchContext(int epoch) {
          try {
              index = new SimpleFSDirectory(Paths.get(System.getProperty("user.dir"), "index", Integer.toString(epoch)));
          } catch (IOException e) {
              throw new RuntimeException(e);
          }
      }

      public IndexSearcher getSearcher() {
          if (searcher == null) {
              try {
                  IndexReader reader = DirectoryReader.open(index);
                  searcher = new IndexSearcher(reader);
              } catch (IOException e) {
                  throw new RuntimeException(e);
              }
          }
          return searcher;
      }

      @Override
      public void close() {}
  }
}
