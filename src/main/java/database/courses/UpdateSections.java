package database.courses;

import static types.Nyu.*;
import static utils.TryCatch.*;

import database.models.SectionID;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.uri.Uri;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.*;
import utils.*;

/**
 * This class insert courses into the Postgresql database based on
 * the data scraped from Albert Mobile
 */
public class UpdateSections {
  private static Logger logger =
      LoggerFactory.getLogger("database.courses.UpdateSections");

  private static final class Prepared implements AutoCloseable {
    final PreparedStatement sections;
    final PreparedStatement descriptions;

    Prepared(Connection conn) throws SQLException {
      this.sections = conn.prepareStatement(
          "UPDATE sections "
          + "SET name = ?, name_vec = to_tsvector(?), "
          + "campus = ?, "
          + "instruction_mode = ?, "
          + "min_units = ?, "
          + "max_units = ?, "
          + "location = ?, "
          + "grading = ?, "
          + "notes = ?, notes_vec = to_tsvector(?), "
          + "prerequisites = ?, prereqs_vec = to_tsvector(?), "
          + "instructors = ? "
          + "WHERE sections.id = ?"
          + "RETURNING sections.course_id");

      this.descriptions = conn.prepareStatement(
          "UPDATE courses SET description = ? WHERE id = ?");
    }

    public void close() throws SQLException {
      this.sections.close();
      this.descriptions.close();
    }
  }

  private static class SaveState {
    SectionID sectionId;
    String data;
  }

  public static void updateSections(Connection conn, Term term,
                                    Iterator<SectionID> sectionIds,
                                    int batchSize) throws SQLException {
    try (Prepared p = new Prepared(conn)) {
      updateSections(p, term, sectionIds, batchSize);
    }
  }

  public static void updateSections(Prepared p, Term term,
                                    Iterator<SectionID> sectionIds,
                                    int batchSize) throws SQLException {
    FutureEngine<SaveState> engine = new FutureEngine<>();

    for (int i = 0; i < batchSize; i++) {
      if (sectionIds.hasNext()) {
        engine.add(query(term, sectionIds.next()));
      }
    }

    // Variables are used to implement exponential backoff over HTTP/1.1
    int inFlight = 5;
    double capacity = 5.0;

    ArrayList<SectionID> failures = new ArrayList<>();

    HashMap<Integer, String> courseDescriptions = new HashMap<>();
    for (SaveState save : engine) {
      inFlight -= 1;

      int max = (int)capacity;

      if (save == null) {
        continue;
      }

      if (save.data == null) {
        failures.add(save.sectionId);

        // And also do some exponential backoff
        if (inFlight <= max) {
          capacity /= 1.5;

          logger.info("Capacity slashed to {} because of timeout", capacity);
        }

        // If we're timing out, let's hold off on adding more requests
        continue;
      }

      if (inFlight < max && failures.isEmpty()) {
        // Steadily increase if we're still succeeding on these requests
        capacity += 0.125;
      }

      while (inFlight < max) {
        Future<SaveState> task = null;
        if (!failures.isEmpty()) {
          task = query(term, failures.remove(failures.size() - 1));
        } else if (sectionIds.hasNext()) {
          task = query(term, sectionIds.next());
        }

        if (task != null) {
          engine.add(task);
          inFlight += 1;
        }
      }

      TryCatch tc =
          tcNew(logger, "Parse error on term={}, registrationNumber={}", term,
                save.sectionId.registrationNumber);

      Section s = tc.pass(() -> parse(save.data));

      logger.debug("Adding section information...");

      PreparedStatement stmt = p.sections;
      Utils.setArray(stmt, s.name,
                     save.sectionId.subjectCode.toString() + ' ' + s.name,
                     s.campus, Utils.nullable(Types.VARCHAR, s.instructionMode),
                     s.minUnits, s.maxUnits, s.location, s.grading,
                     Utils.nullable(Types.VARCHAR, s.notes),
                     Utils.nullable(Types.VARCHAR, s.notes),
                     Utils.nullable(Types.VARCHAR, s.prerequisites),
                     Utils.nullable(Types.VARCHAR, s.prerequisites),
                     s.instructors, save.sectionId.id);
      stmt.execute();

      ResultSet rs = stmt.getResultSet();
      if (!rs.next())
        throw new RuntimeException("why");

      int courseId = rs.getInt(1);
      rs.close();

      if (s.description != null && !courseDescriptions.containsKey(courseId)) {
        courseDescriptions.put(courseId, s.description);
      }
    }

    PreparedStatement stmt = p.descriptions;
    for (Map.Entry<Integer, String> entry : courseDescriptions.entrySet()) {
      Utils.setArray(stmt, entry.getValue(), entry.getKey());

      if (stmt.executeUpdate() == 0)
        throw new RuntimeException("why did this fail?");
    }
  }

  private static final String ROOT_URL =
      "https://m.albert.nyu.edu/app/catalog/classSearch";
  private static String DATA_URL_STRING =
      "https://m.albert.nyu.edu/app/catalog/classsection/NYUNV/";

  private static Future<SaveState> query(Term term, SectionID sectionId) {
    int registrationNumber = sectionId.registrationNumber;
    if (registrationNumber < 0) {
      throw new IllegalArgumentException("Registration numbers are positive");
    }

    logger.debug("Querying section in term={} with registrationNumber={}", term,
                 registrationNumber);

    Request request =
        new RequestBuilder()
            .setUri(Uri.create(DATA_URL_STRING + term.getId() + "/" +
                               registrationNumber))
            .setHeader("Referer", ROOT_URL + "/" + term.getId())
            .setHeader("Host", "m.albert.nyu.edu")
            .setHeader("Accept-Language", "en-US,en;q=0.5")
            .setHeader("Accept-Encoding", "gzip, deflate, br")
            .setHeader("Content-Type",
                       "application/x-www-form-urlencoded; charset=UTF-8")
            .setHeader("X-Requested-With", "XMLHttpRequest")
            .setHeader("Origin", "https://m.albert.nyu.edu")
            .setHeader("DNT", "1")
            .setHeader("Connection", "keep-alive")
            .setHeader("Referer",
                       "https://m.albert.nyu.edu/app/catalog/classSearch")
            .setRequestTimeout(20000)
            .setMethod("GET")
            .build();

    return Client.send(request, (resp, throwable) -> {
      SaveState state = new SaveState();
      state.sectionId = sectionId;

      if (throwable instanceof io.netty.channel.ConnectTimeoutException) {
        return state;
      }

      if (throwable instanceof java.util.concurrent.TimeoutException) {
        return state;
      }

      if (throwable instanceof java.net.ConnectException) {
        return state;
      }

      if (resp == null) {
        logger.error("Querying section failed: term={}, registrationNumber={}",
                     term, registrationNumber, throwable);

        return null;
      }

      state.data = resp.getResponseBody();

      return state;
    });
  }

  private static Pattern pattern = Pattern.compile("[0-9]");

  private static Map<String, String> buildings;

  static {
    buildings = new HashMap<>();
    for (String line : Utils.asResourceLines("/building.txt")) {
      String[] entry = line.split(",", 2);

      buildings.put(entry[0], entry[1]);
    }
  }

  public static Section parse(String rawData) {
    logger.debug("parsing raw catalog section data into Section...");

    rawData = rawData.trim();

    if (rawData.equals("")) {
      // the course doesn't exist
      throw new RuntimeException("Got bad data: empty string");
    }

    Document doc = Jsoup.parse(rawData);
    Element failed = doc.selectFirst("div.alert.alert-info");
    if (failed != null) {
      logger.error("Got bad data: {}", failed.text());

      throw new RuntimeException("Got bad data");
    }

    Elements elements = doc.select("a");
    String link = null;
    for (Element element : elements) {
      String el = element.attr("href");

      if (el.contains("mapBuilding")) {
        link = el;
      }
    }

    doc.select("a").unwrap();
    doc.select("i").unwrap();
    doc.select("b").unwrap();

    Element outerDataSection = doc.selectFirst("body > section.main");
    Element innerDataSection = outerDataSection.selectFirst("> section");
    Element courseNameDiv = innerDataSection.selectFirst("> div.primary-head");

    if (courseNameDiv == null) {
      logger.info("Crashing with data={}", rawData);

      throw new RuntimeException("What happened here?");
    }

    String courseName = courseNameDiv.text();

    Elements dataDivs =
        innerDataSection.select("> div.section-content.clearfix");

    Map<String, String> secData = parseSectionAttributes(dataDivs);

    return parsingElements(secData, courseName, link);
  }

  static Map<String, String> parseSectionAttributes(Elements attributeData) {
    Map<String, String> map = new HashMap<>();
    for (Element e : attributeData) {
      if (e.child(0).text().equals("Topic") &&
          e.child(1).text().contains("Room")) {
        continue;
      }

      map.put(e.child(0).text(), e.child(1).wholeText().trim());
    }

    return map;
  }

  public static Section parsingElements(Map<String, String> secData,
                                        String sectionName, String link) {
    String units = secData.get("Units");
    double minUnits = 0, maxUnits;
    if (units.contains("-")) {
      minUnits = Float.parseFloat(units.split(" - ")[0]);
      maxUnits = Float.parseFloat(units.split(" - ")[1].split(" ")[0]);
    } else {
      maxUnits = Float.parseFloat(units.split(" ")[0]);
    }

    sectionName +=
        secData.containsKey("Topic") ? " " + secData.get("Topic") : "";

    parseBuilding(secData, link);

    String[] instructors = secData.get("Instructor(s)").split(", *\\n *\\n");

    Section s = new Section();
    s.name = sectionName.equals("") ? null : sectionName;
    s.registrationNumber = Integer.parseInt(secData.get("Class Number"));
    s.status = SectionStatus.parseStatus(secData.get("Status"));
    s.campus = secData.get("Location");
    s.description = secData.get("Description");
    s.instructionMode = secData.get("Instruction Mode");
    s.instructors = instructors;
    s.minUnits = minUnits;
    s.maxUnits = maxUnits;
    s.grading = secData.get("Grading");
    s.notes = secData.getOrDefault("Notes", null);
    s.prerequisites = secData.getOrDefault("Enrollment Requirements", null);
    s.location = secData.get("Room");

    return s;
  }

  public static void parseBuilding(Map<String, String> secData, String link) {
    String location = secData.get("Room");
    String room = "";
    String building = null;

    if (location.contains("Loc") || location.contains("Loc:")) {
      location = location.split("Loc")[0];
      location = location.trim();
      if (pattern.matcher(location).find()) {
        if (location.contains("Rm:")) {
          String[] arrs = location.split("Rm:");
          if (arrs.length == 2) {
            room = arrs[1];
          }
        } else if (location.contains("Rm")) {
          String[] arrs = location.split("Rm");
          if (arrs.length == 2) {
            room = arrs[1];
          }
        } else if (location.contains("Room:")) {
          String[] arrs = location.split("Room:");
          if (arrs.length == 2) {
            room = arrs[1];
          }
        } else if (location.contains("Room")) {
          String[] arrs = location.split("Room");
          if (arrs.length == 2) {
            room = arrs[1];
          }
        }
      }

      if (link != null) {
        link = link.substring(link.lastIndexOf("/") + 1);
        if (buildings.containsKey(link)) {
          building = buildings.get(link);
        }
      }

      if (!room.equals("") && building != null) {
        secData.put("Room", building + " - Room:" + room);
      } else {
        secData.put("Room", location);
      }
    }
  }
}
