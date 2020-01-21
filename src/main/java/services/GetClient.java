package services;

import org.asynchttpclient.AsyncHttpClient;
import static org.asynchttpclient.Dsl.*;
import java.io.IOException;

/**
 This class get connection to the Postgresql database using JDBC Driver
 */
public class GetClient {

    private static AsyncHttpClient client;

    public static AsyncHttpClient getClient() {
        if (client == null) {
            client = asyncHttpClient();
        }
        return client;
    }

    public static void close() throws IOException {
        if (client != null)
            client.close();
        client = null;
    }
}