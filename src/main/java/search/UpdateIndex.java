package search;

import java.io.IOException;
import java.util.stream.Stream;

import database.models.CourseSectionRow;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;

public class UpdateIndex {

  public static void
  updateIndex(int epoch,
              Stream<CourseSectionRow> rows) {
    IndexWriter writer =
        GetResources.getWriter(epoch);
    rows.forEach(row -> {
      Document doc = new Document();
      doc.add(new TextField("name",
                            row.sectionName == null ? "" : row.sectionName,
                            Field.Store.YES));
      doc.add(new TextField("description",
                            row.description == null ? "" : row.description,
                            Field.Store.YES));
      doc.add(new TextField("id", Integer.toString(row.sectionId),
                            Field.Store.YES));

      String instructors = row.instructors == null
                               ? "Staff"
                               : String.join(" ; ", row.instructors);
      doc.add(new TextField("instructors", instructors, Field.Store.YES));

      try {
        writer.addDocument(doc);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    try {
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
