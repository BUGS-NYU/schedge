package api;

import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import scraping.models.SchoolMetadata;
import scraping.models.SubjectCode;
import org.jetbrains.annotations.NotNull;

class SchoolsEndpoint extends Endpoint {

  @NotNull
  @Override
  String getPath() {
    return "/schools";
  }

  @NotNull
  @Override
  OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          // openApiOperation.operationId("Operation Id");
          openApiOperation.description(
              "This endpoint returns a list of schools.");
          openApiOperation.summary("Schools Endpoint");
        })
        .jsonArray("200", SchoolMetadata.class,
                   openApiParam -> { openApiParam.description("OK."); });
  }

  @NotNull
  @Override
  Handler getHandler() {
    return ctx -> { ctx.json(SubjectCode.getAllSchools()); };
  }
}
