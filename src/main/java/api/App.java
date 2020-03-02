package api;

import io.javalin.Javalin;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import io.swagger.v3.oas.models.info.Info;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Utils;

public class App {

  private static final Logger logger = LoggerFactory.getLogger("api.App");

  public static void run() {
    // @TODO Use https://javalin.io/plugins/openapi#getting-started as guide for
    // documenting API, then use Redoc for docs generation
    Javalin app =
        Javalin
            .create(config -> {
              config.enableCorsForAllOrigins();
              config.server(App::createServer);
              String description =
                  "Schedge is an API to NYU's course catalog. "
                  + "Please note that <b>this API is currently under "
                  + "active development and is subject to change</b>."
                  + "<br/> <br/> If you'd like to contribute, "
                  +
                  "<a href=\"https://github.com/A1Liu/schedge\">check out the repository</a>.";
              Info info =
                  new Info().version("1.0").title("Schedge").description(
                      description);
              OpenApiOptions options =
                  new OpenApiOptions(info).path("/swagger.json");
              config.enableWebjars();
              config.registerPlugin(new OpenApiPlugin(options));
            })
            .start();
    Logger logger = LoggerFactory.getLogger("app");

    String docs = new BufferedReader(
                      new InputStreamReader(
                          Javalin.class.getResourceAsStream("/index.html")))
                      .lines()
                      .collect(Collectors.joining("\n"));

    app.get("/", OpenApiBuilder.documented(OpenApiBuilder.document().ignore(),
                                           ctx -> {
                                             ctx.contentType("text/html");
                                             ctx.result(docs);
                                           }));

    new SubjectsEndpoint().addTo(app);
    new SchoolsEndpoint().addTo(app);
    new CoursesEndpoint().addTo(app);
  }

  // Copied directly from Javalin example
  private static Server createServer() {
    Server server = new Server();

    ServerConnector connector = new ServerConnector(server);
    connector.setPort(80);
    server.addConnector(connector);

    // HTTP Configuration
    HttpConfiguration httpConfig = new HttpConfiguration();
    httpConfig.setSendServerVersion(false);
    httpConfig.setSecureScheme("https");
    httpConfig.setSecurePort(443);

    // SSL Context Factory for HTTPS and HTTP/2

    SslContextFactory sslContextFactory = new SslContextFactory.Server();

    URL resource = Utils.class.getResource("/keystore.jks");
    if (resource == null) {
      logger.info("Couldn't find keystore at src/main/resources/keystore.jks");
      return server;
    }

    sslContextFactory.setKeyStorePath(
        resource.toExternalForm()); // replace with your real keystore
    sslContextFactory.setKeyStorePassword(
        ""); // replace with your real password
    sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);
    sslContextFactory.setProvider("Conscrypt");

    // HTTPS Configuration
    HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
    httpsConfig.addCustomizer(new SecureRequestCustomizer());

    // HTTP/2 Connection Factory
    HTTP2ServerConnectionFactory h2 =
        new HTTP2ServerConnectionFactory(httpsConfig);
    ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
    alpn.setDefaultProtocol("h2");

    // SSL Connection Factory
    SslConnectionFactory ssl =
        new SslConnectionFactory(sslContextFactory, alpn.getProtocol());

    // HTTP/2 Connector
    ServerConnector http2Connector = new ServerConnector(
        server, ssl, alpn, h2, new HttpConnectionFactory(httpsConfig));
    http2Connector.setPort(443);
    server.addConnector(http2Connector);

    return server;
  }
}

abstract class Endpoint {

  @NotNull abstract String getPath();

  @NotNull
  abstract OpenApiDocumentation configureDocs(OpenApiDocumentation docs);

  @NotNull abstract Handler getHandler();

  public final void addTo(Javalin app) {
    app.get(getPath(),
            OpenApiBuilder.documented(configureDocs(OpenApiBuilder.document()),
                                      getHandler()));
  }
}

class ApiError {
  // private int status;
  private String message;

  ApiError(String message) {
    // this.status = status;
    this.message = message;
  }

  // public int getStatus() { return status; }

  public String getMessage() { return message; }
}
