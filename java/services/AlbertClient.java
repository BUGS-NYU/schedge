package services;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

import io.netty.handler.codec.http.cookie.Cookie;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import models.SubjectCode;
import models.Term;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.asynchttpclient.cookie.CookieStore;
import org.asynchttpclient.cookie.ThreadSafeCookieStore;
import org.asynchttpclient.uri.Uri;
import org.slf4j.Logger;

class AlbertClient {

  private static String ROOT_URL =
      "https://m.albert.nyu.edu/app/catalog/classSearch";
  private static String DATA_URL =
      "https://m.albert.nyu.edu/app/catalog/getClassSearch";

  private Logger logger;
  private AsyncHttpClient client;
  private CookieStore cookieStore;
  private String csrfToken;
//  private BlockingQueue<AlbertFuture> idle;
//  private BlockingQueue<AlbertFuture> running;
//  private BlockingQueue<AlbertResult> results;

  AlbertClient(Logger logger) throws IOException {

    this.logger = logger;
    cookieStore = new ThreadSafeCookieStore();

    client = asyncHttpClient(config().setCookieStore(cookieStore).build());
    try {
      client.prepareGet(ROOT_URL)
          .execute()
          .get(); // We need the CSRF token from this.
      List<Cookie> cookies =
          client.getConfig().getCookieStore().get(Uri.create(ROOT_URL));
      for (Cookie cookie : cookies) {
        if (cookie.name().equals("CSRFCookie")) {
          csrfToken = cookie.value();
          logger.info("Client instance created with CSRF Token '{}'.",
                      csrfToken);
          break;
        }
      }

      if (csrfToken == null) {
        logger.error("Couldn't find `CSRFCookie`. Cookies found were {}.",
                     cookies);
        throw new IOException("NYU servers did something unexpected.");
      }
    } catch (Exception e) {
      logger.error(
          "Couldn't get CSRF token from root URL of Albert, error occured with message '{}'.",
          e.getMessage());
      throw new IOException(e);
    }
  }

  void close() throws IOException { this.client.close(); }

  LabelledFuture requestCatalogData(Term term, SubjectCode subjectCode)
      throws InterruptedException {
    logger.info("querying catalog for term={} and subject={}...", term,
                subjectCode);
    ListenableFuture<Response> response =
        client.preparePost(DATA_URL)
            .addFormParam("CSRFToken", csrfToken)
            .addFormParam("term", Integer.toString(term.getId()))
            .addFormParam("acad_group", subjectCode.getSchool())
            .addFormParam("subject", subjectCode.getAbbrev())
            .addHeader("Referrer", ROOT_URL + "/" + term.getId())
            .addHeader("Host", "m.albert.nyu.edu")
            .execute();

    Thread.sleep(100);

    return new LabelledFuture(subjectCode, response);
  }

  public static class AlbertResult {
    SubjectCode subject;
    String data;

    AlbertResult(SubjectCode subject, String data) {
      this.subject = subject;
      this.data = data;
    }

    public SubjectCode component1() { return subject; }

    public String component2() { return data; }
  }

  public static class AlbertFuture {
    Logger logger;
    AsyncHttpClient client;
    String csrfToken;
    Term term;
    SubjectCode subject;
    ListenableFuture<Response> response = null;

    AlbertFuture(Logger logger, Term term, SubjectCode subject)
        throws IOException {
      this.term = term;
      this.subject = subject;
      this.logger = logger;

      logger.debug("Creating client instance...");
      CookieStore cookieStore = new ThreadSafeCookieStore();
      this.client =
          asyncHttpClient(config().setCookieStore(cookieStore).build());

      try {
        client.prepareGet(ROOT_URL)
            .execute()
            .get(); // We need the CSRF token from this.
        List<Cookie> cookies =
            client.getConfig().getCookieStore().get(Uri.create(ROOT_URL));
        for (Cookie cookie : cookies) {
          if (cookie.name().equals("CSRFCookie")) {
            csrfToken = cookie.value();
            logger.info("Client instance created with CSRF Token '{}'.",
                        csrfToken);
            break;
          }
        }

        if (csrfToken == null) {
          logger.error("Couldn't find `CSRFCookie`. Cookies found were {}.",
                       cookies);
          throw new IOException("NYU servers did something unexpected.");
        }
      } catch (Exception e) {
        logger.error(
            "Couldn't get CSRF token from root URL of Albert, error occured with message '{}'.",
            e.getMessage());
        throw new IOException(e);
      }

      this.response = startFuture();
    }

    private AlbertFuture(AlbertFuture other) {
      this.client = other.client;
      this.subject = other.subject;
      this.term = term;
      this.csrfToken = other.csrfToken;
      this.logger = other.logger;
      this.response = startFuture();
    }

    private ListenableFuture<Response> startFuture() {
      return client.preparePost(DATA_URL)
          .addFormParam("CSRFToken", csrfToken)
          .addFormParam("term", Integer.toString(term.getId()))
          .addFormParam("acad_group", subject.getSchool())
          .addFormParam("subject", subject.getAbbrev())
          .addHeader("Referrer", ROOT_URL + "/" + term.getId())
          .addHeader("Host", "m.albert.nyu.edu")
          .execute();
    }

    AlbertFuture reuseFuture(SubjectCode newSubject) {
      if (response != null)
        throw new AssertionError("This future hasn't completed yet!");
      return new AlbertFuture(this);
    }

    void close() throws IOException {
        this.client.close();
    }

    AlbertResult get() throws ExecutionException, InterruptedException,
                              NoSuchElementException {
      if (response == null)
        throw new NoSuchElementException("Already used this future!");
      String result = this.response.get().getResponseBody();
      this.response = null;
      return new AlbertResult(this.subject, result);
    }
  }

  static class LabelledFuture {
    // AsyncHttpClient client;
    SubjectCode subject;
    ListenableFuture<Response> response;

    LabelledFuture(SubjectCode subject, ListenableFuture<Response> response) {
      this.subject = subject;
      this.response = response;
    }

    QueryCatalog.LabelledResult get()
        throws ExecutionException, InterruptedException {
      return new QueryCatalog.LabelledResult(subject,
                                             response.get().getResponseBody());
    }
  }
}
