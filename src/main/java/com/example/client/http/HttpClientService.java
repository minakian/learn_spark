package com.example.client.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpClientService {
    /**
     * Sends an HTTP GET request to the specified endpoint.
     */
    public static void sendGetRequest(String endpoint) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(endpoint))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("GET response: " + response.body());
        } catch (Exception e) {
            System.err.println("GET request error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends an HTTP POST request with a JSON payload to the specified endpoint.
     */
    public static void sendPostRequest(String endpoint, String jsonPayload) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("POST response: " + response.body());
        } catch (Exception e) {
            System.err.println("POST request error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
