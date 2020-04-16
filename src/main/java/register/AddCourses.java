package register;

import org.asynchttpclient.uri.Uri;

/**
 * Beta: Testing registering for courses, adding to shopping cart portion
 */
public class AddCourses {
    //We will do undergrad for now/ Remember to add term and registration number at the end
    private static final String SHOPPING_CART_DATA_URL_STRING =
            "https://m.albert.nyu.edu/app/student/enrollmentcart/addToCart/NYUNV/UGRD";
    private static final String SHOPPING_CART_ROOT_URL_STRING =
            "https://m.albert.nyu.edu/app/student/enrollmentcart/cart";

    private static final Uri SHOPPING_CART_ROOT_URI =
            Uri.create(SHOPPING_CART_ROOT_URL_STRING);

//    public static CompletableFuture<String> addCourseToCart(
//            String username, String password,
//            Term term, int registrationNumber, Context.HttpContext preContext) {
//        List<Cookie> cookies = Login.getLoginSession(username, password, preContext);
//        System.out.println(cookies.toString());
//        Request request =
//                new RequestBuilder()
//                        .setUri(Uri.create(SHOPPING_CART_DATA_URL_STRING + "/" + term.getId() + "/" + registrationNumber))
//                        .setRequestTimeout(60000)
//                        .setHeader("Referer", SHOPPING_CART_ROOT_URL_STRING)
//                        .setHeader("Host", "m.albert.nyu.edu")
//                        .setHeader("Accept-Language", "en-US,en;q=0.5")
//                        .setHeader("Accept-Encoding", "gzip, deflate, br")
//                        .setHeader("Content-Type",
//                                "application/x-www-form-urlencoded; charset=UTF-8")
//                        .setHeader("X-Requested-With", "XMLHttpRequest")
//                        .setHeader("Origin", "https://m.albert.nyu.edu")
//                        .setHeader("DNT", "1")
//                        .setHeader("Connection", "keep-alive")
//                        .setHeader("Cookie", cookies.stream()
//                                .map(it -> it.name() + '=' + it.value())
//                                .collect(Collectors.joining("; ")))
//                        .setMethod("GET")
//                        .build();
//        GetClient.getClient()
//                .executeRequest(request)
//                .toCompletableFuture()
//                .handleAsync(((resp, throwable) -> {
//                    System.out.println(resp.getStatusCode());
//                    return null;
//                }));
//        return null;
//    }
}
