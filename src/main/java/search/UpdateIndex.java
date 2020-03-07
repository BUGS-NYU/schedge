package search;

import database.GetConnection;
import database.SelectCourseSectionRows;
import java.io.IOException;
import java.sql.Connection;
import java.util.stream.Stream;
import nyu.SubjectCode;
import nyu.Term;
import org.apache.lucene.document.*;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

public class UpdateIndex {

  public static void
  updateIndex(int epoch,
              Stream<SelectCourseSectionRows.CourseSectionRow> rows) {
    GetResources.SearchContext searchContext =
        GetResources.getSearchContext(epoch);
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
      doc.add(new StringField("subject", row.subject.code, Field.Store.YES));
      doc.add(new StringField("school", row.subject.school, Field.Store.YES));

      try {
        searchContext.writer.addDocument(doc);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    try {
      searchContext.writer.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
