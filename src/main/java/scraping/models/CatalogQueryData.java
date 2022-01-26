package scraping.models;

import nyu.SubjectCode;

public class CatalogQueryData {

  private SubjectCode subject;
  private String data;

  public CatalogQueryData(SubjectCode subject, String data) {
    this.subject = subject;
    this.data = data;
  }

  public SubjectCode getSubject() { return subject; }

  public String getData() { return data; }

  public String toString() { return getData(); }
}
