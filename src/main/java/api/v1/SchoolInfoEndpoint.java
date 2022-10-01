package api.v1;

import static database.GetConnection.withConnectionReturning;
import static database.SelectSubjects.*;

import api.*;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.*;

import types.Nyu;

public final class SchoolInfoEndpoint extends App.Endpoint {
  public String getPath() { return "/schools/{term}"; }

  public final class SubjectInfo {
    public String code;
    public String name;
  }

  public final class SchoolInfo {
    public String code;
    public String name;
    public ArrayList<SubjectInfo> subjects;
  }

  public final class Info {
    public Nyu.Term term;
    public HashMap<String, SchoolInfo> schools;
  }

  public static final String TERM_PARAM_DESCRIPTION =
      "Must be a valid term code, either 'current', 'next', or something "
      + "like sp2021 for Spring 2021. Use 'su' for Summer, 'sp' "
      + "for Spring, 'fa' for Fall, and 'ja' for January/JTerm";

  public static Nyu.Term parseTerm(String termString) {
    if (termString.contentEquals("current")) {
      return Nyu.Term.getCurrentTerm();
    }

    if (termString.contentEquals("next")) {
      return Nyu.Term.getCurrentTerm().nextTerm();
    }

    int year = Integer.parseInt(termString.substring(2));
    return new Nyu.Term(termString.substring(0, 2), year);
  }

  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          openApiOperation.description(
              "This endpoint provides general information on the subjects in a term");
          openApiOperation.summary("Schools and Subjects");
        })
        .pathParam("term", String.class,
                   openApiParam -> {
                     openApiParam.description(TERM_PARAM_DESCRIPTION);
                   })
        .json("200", Info.class,
              openApiParam -> { openApiParam.description("OK."); });
  }

  public Object handleEndpoint(Context ctx) {
    Nyu.Term term = parseTerm(ctx.pathParam("term"));

    Info info = new Info();
    info.term = term;
    info.schools = withConnectionReturning(conn -> {
      ArrayList<Subject> subjects = selectSubjectsForTerm(conn, term);
      ArrayList<School> schools = selectSchoolsForTerm(conn, term);

      HashMap<String, ArrayList<SubjectInfo>> subjectsInfo = new HashMap<>();
      for (Subject subject : subjects) {
        SubjectInfo subjectInfo = new SubjectInfo();
        subjectInfo.name = subject.name;
        subjectInfo.code = subject.code;

        subjectsInfo.computeIfAbsent(subject.school, k -> new ArrayList<>())
            .add(subjectInfo);
      }

      ArrayList<SubjectInfo> empty = new ArrayList<>();
      HashMap<String, SchoolInfo> schoolsInfo = new HashMap<>();

      for (School school : schools) {
        SchoolInfo schoolInfo = new SchoolInfo();
        schoolInfo.code = school.code;
        schoolInfo.name = school.name;
        schoolInfo.subjects = subjectsInfo.getOrDefault(school.code, empty);

        schoolsInfo.put(schoolInfo.code, schoolInfo);
      }

      return schoolsInfo;
    });

    return info;
  }
}
