package api;

import io.javalin.Javalin;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;

public abstract class Endpoint {
  public abstract String getPath();
  public abstract OpenApiDocumentation configureDocs(OpenApiDocumentation docs);
  public abstract Handler getHandler();
  public final void addTo(Javalin app) {
    app.get("/api" + getPath(),
            OpenApiBuilder.documented(configureDocs(OpenApiBuilder.document()),
                                      getHandler()));
  }
}