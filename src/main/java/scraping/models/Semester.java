package scraping.models;

public enum Semester {
  January(2),
  Spring(4),
  Summer(6),
  Fall(8);

  private Integer id;

  Semester(Integer id) { this.id = id; }

  public static Semester fromCode(String code) {
    Semester sem;
    switch (code) {
    case "ja":
      sem = January;
      break;
    case "fa":
      sem = Fall;
      break;
    case "sp":
      sem = Spring;
      break;
    case "su":
      sem = Summer;
      break;
    default:
      sem = valueOf(code);
    }
    return sem;
  }

  public static Semester fromInt(Integer id) {
    Semester sem;
    switch (id) {
    case 2:
      sem = January;
      break;
    case 4:
      sem = Spring;
      break;
    case 6:
      sem = Summer;
      break;
    case 8:
      sem = Fall;
      break;
    default:
      throw new IllegalArgumentException(
          "ID can only be one of {2,4,6,8} (got " + id + ")");
    }
    return sem;
  }

  public Integer toInt() { return this.id; }
}
