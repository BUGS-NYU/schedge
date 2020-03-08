package search;

import database.models.CourseSectionRow;
import java.io.IOException;
import java.util.stream.Stream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

public class UpdateIndex {

  public static void updateIndex(int epoch, Stream<CourseSectionRow> rows) {
    if (GetResources.alreadyUpdated(epoch))
      return;
    IndexWriter writer = GetResources.getWriter(epoch);
    rows.forEach(row -> {
      Document doc = new Document();
      Field nameField =
          new TextField("name", row.sectionName == null ? "" : row.sectionName,
                        Field.Store.YES);
      doc.add(nameField);
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
