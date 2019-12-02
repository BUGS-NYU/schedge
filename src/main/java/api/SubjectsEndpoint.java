package api;

import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.jetbrains.annotations.NotNull;
import models.SubjectCode;

class SubjectsEndpoint extends Endpoint {

  @NotNull
  @Override
  String getPath() {
    return "/subjects";
  }

  @NotNull
  @Override
  OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs.jsonArray("200", SubjectCode.class);
  }

  @NotNull
  @Override
  Handler getHandler() {
    return ctx -> { ctx.json(SubjectCode.allSubjects()); };
  }
}
