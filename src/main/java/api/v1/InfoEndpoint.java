package api.v1;

import api.*;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.*;
import types.*;

public final class InfoEndpoint extends Endpoint {
  public String getPath() { return "/info"; }

  public final class Info {
    public Term currentTerm;
    public Map<String, Subject.School> schools;
    public Map<String, String> subjectNames;
  }

  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          openApiOperation.description(
              "This endpoint provides general information on schools, subjects, etc.");
          openApiOperation.summary("Information Endpoint");
        })
        .json("200", Info.class,
              openApiParam -> { openApiParam.description("OK."); });
  }

  public Handler getHandler() {
    return ctx -> {
      Info info = new Info();
      info.currentTerm = Term.getCurrentTerm();
      info.schools = Subject.allSchools();
      info.subjectNames = new HashMap<>();

      for (Subject subject : Subject.allSubjects()) {
        info.subjectNames.put(subject.code, subject.name);
      }

      ctx.status(200);
      ctx.json(info);
    };
  }
}
