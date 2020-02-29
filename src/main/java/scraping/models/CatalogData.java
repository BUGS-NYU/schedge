package scraping.models;
import models.SubjectCode;

public class CatalogData {

  private SubjectCode subject;
  private String data;

  public CatalogData(SubjectCode subject, String data) {
    this.subject = subject;
    this.data = data;
  }

  public SubjectCode getSubject() { return subject; }

  public String getData() { return data; }
}
