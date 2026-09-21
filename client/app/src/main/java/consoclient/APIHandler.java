package consoclient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class APIHandler {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static String SERVER_URL;
    private static String TOKEN;

    public static void setServerUrl(String url) {
        SERVER_URL = url;
    }

    public static void setToken(String token) {
        TOKEN = token;
    }

    public static boolean checkStatus() {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(SERVER_URL + "/status")).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("Could not reach server: " + e.getMessage());
            return false;
        }
    }
}
