package types;

import java.util.*;

public final class Nyu {
  public static final class Subject {
    public final String code;
    public final String name;

    public Subject(String code, String name) {
      this.code = code;
      this.name = name;
    }
  }

  public static final class School {
    public final String name;
    public final ArrayList<Subject> subjects;

    public School(String name) {
      this.name = name;
      this.subjects = new ArrayList<>();
    }
  }
}
