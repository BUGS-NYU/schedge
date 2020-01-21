package scraping;

import models.SubjectCode;
import models.Term;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
    @Todo: Add annotation for parameter. Fix the method to parse
    @Help: Add annotations, comments to code
 */
public class QueryCatalog {
    private static Logger logger = LoggerFactory.getLogger("scraping.catalog");
    private static String ROOT_URl = "https://m.albert.nyu.edu/app/catalog/classSearch";
    private static String DATA_URL = "https://m.albert.nyu.edu/app/catalog/getClassSearch";

    public static String queryCatalog(Term term, SubjectCode subjectCode)
            throws ExecutionException, InterruptedException {
        String result = queryCatalog(term, subjectCode, getHttpContext()).get();
        return result;
    }

    public static Vector<String> queryCatalog(
            Term term, List<SubjectCode> subjectCodes, Integer batchSizeNullable) {
        if (subjectCodes.size() > 1) {
            logger.info("querying catalog for term = " + term.toString() + " with mutiple subjects");
        }
        Integer batchSize =
                batchSizeNullable != null
                        ? batchSizeNullable
                        : Math.max(5, Math.min(subjectCodes.size() / 5, 20));

        Vector<HttpContext> contexts = new Vector<>();
        IntStream.range(0, batchSize)
                .forEach(
                        idx -> {
                            try {
                                contexts.add(getContextAsync().get());
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        });

        Vector<String> outputs = new Vector<>();
        new SimpleBatchedFutureEngine<>(
            subjectCodes, batchSize,
            ((subjectCode, integer)
                 -> queryCatalog(term, subjectCode, contexts.get(integer))))
            .forEachRemaining(outputs::add);
        return outputs;
    }

    private static Future<String> queryCatalog(
            Term term, SubjectCode subjectCode, HttpContext httpContext) {
        logger.info(
                "querying catalog for term= " + term.getId() + " and subject= " + subjectCode.toString());
        CompletableFuture<String> future = new CompletableFuture<>();
        CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
        try {
            client.start();
            Map<String, String> map = new LinkedHashMap<>();
            map.put("CSRFToken", httpContext.csrfToken);
            map.put("term", String.valueOf(term.getId()));
            map.put("acad_group", subjectCode.getSchool());
            map.put("subject", subjectCode.toString());
            HttpPost postRequest = new HttpPost(DATA_URL + "/" + term.getId());
            postRequest.setHeader("Referrer", ROOT_URl + "/" + term.getId());
            postRequest.setHeader("Host", "m.albert.nyu.edu");
            postRequest.setHeader("Accept-Language", "en-US,en;q=0.5");
            postRequest.setHeader("Accept-Encoding", "gzip, deflate, br");
            postRequest.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            postRequest.setHeader("X-Requested-With", "XMLHttpRequest");
            postRequest.setHeader("Origin", "https://m.albert.nyu.edu");
            postRequest.setHeader("DNT", "1");
            postRequest.setHeader("Connection", "keep-alive");
            postRequest.setHeader("Referer", "https://m.albert.nyu.edu/app/catalog/classSearch");
            postRequest.setHeader(
                    "Cookie",
                    httpContext.cookies.stream()
                            .map(value -> value.getName() + "=" + value.getValue())
                            .collect(Collectors.joining(";")));
            String values =
                    map.entrySet().stream()
                            .map(value -> value.getKey() + "=" + value.getValue())
                            .collect(Collectors.joining("&"));
            postRequest.setEntity(new StringEntity(values));

            HttpResponse response = client.execute(postRequest, null).get();
            String result = EntityUtils.toString(response.getEntity());
            if (result.equals("No classes found matching your criteria.")) {
                logger.warn(
                        "No classes found matching criteria school = "
                                + subjectCode.getSchool()
                                + " & subject = "
                                + subjectCode.getSubject());
                future.complete(null);
            } else {
                future.complete(result);
            }
            client.close();
        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
        }
        return future;
    }

    private static HttpContext getHttpContext() throws ExecutionException, InterruptedException {
        return getContextAsync().get();
    }

    private static Future<HttpContext> getContextAsync() {
        logger.info("Getting CSRF token ....");
        CompletableFuture<HttpContext> future = new CompletableFuture<>();
        HttpClientContext context = HttpClientContext.create();
        CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
        try {
            client.start();
            HttpGet request = new HttpGet(ROOT_URl);
            client.execute(request, context, null).get();
            List<Cookie> cookies = context.getCookieStore().getCookies();
            String csrfToken =
                    Objects.requireNonNull(
                            cookies.stream()
                                    .filter(value -> value.getName().equals("CSRFCookie"))
                                    .findAny()
                                    .orElse(null))
                            .getValue();
            future.complete(new HttpContext(csrfToken, cookies));
            logger.info("Retrieved CSRF token " + csrfToken);
            client.close();
        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
        }
        return future;
    }

    private static class HttpContext {
        private String csrfToken;
        private List<Cookie> cookies;

        public HttpContext(String csrfToken, List<Cookie> cookies) {
            this.csrfToken = csrfToken;
            this.cookies = cookies;
        }

        public String toString() {
            return "csrfToken = " + csrfToken + " & cookies = " + cookies;
        }
    }
}
