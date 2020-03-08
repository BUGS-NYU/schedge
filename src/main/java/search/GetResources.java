package search;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetResources {

  public final static Analyzer analyzer = new StandardAnalyzer();
  public static IndexWriterConfig config = new IndexWriterConfig(analyzer);
  // @Performance We need to eventually be able to delete indices, using
  // AtomicInteger to be thread safe
  private static HashMap<Integer, SearchContext> contexts = new HashMap<>();
  private static Logger logger = LoggerFactory.getLogger("search.GetResources");

  public static Path getIndexPathForEpoch(int epoch) {
    return Paths.get(System.getProperty("user.dir"), "index",
                     Integer.toString(epoch));
  }

  public static File getIndexFileForEpoch(int epoch) {
    return new File(String.valueOf(getIndexPathForEpoch(epoch)));
  }

  public static boolean alreadyUpdated(int epoch) {
    return getIndexFileForEpoch(epoch).exists();
  }

  private static SearchContext getSearchContext(int epoch) {
    synchronized (contexts) {
      if (contexts.containsKey(epoch) && alreadyUpdated(epoch)) {
        return contexts.get(epoch);
      } else {
        SearchContext context = new SearchContext(epoch);
        contexts.put(epoch, context);
        return context;
      }
    }
  }

  public static void closeIndex(int epoch) {
    synchronized (contexts) {
      if (contexts.containsKey(epoch)) {
        contexts.remove(epoch);
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
    if (alreadyUpdated(epoch)) {
      return getSearchContext(epoch).getSearcher();
    }
    logger.warn("Writer hasn't built index yet for reader of epoch=" + epoch);
    return null;
  }

  private static class SearchContext implements AutoCloseable {
    private final Directory index;
    private IndexSearcher searcher = null;

    SearchContext(int epoch) {
      try {
        index = new SimpleFSDirectory(getIndexPathForEpoch(epoch));
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
