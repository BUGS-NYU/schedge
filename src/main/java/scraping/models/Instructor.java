package scraping.models;

import java.util.ArrayList;
import java.util.List;

public final class Instructor {
  public final int id;
  public final String name;


  public Instructor(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public String toString() {
    return id + ". " + name;
  }
}
