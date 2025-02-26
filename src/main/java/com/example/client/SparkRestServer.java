package com.example.client;

import static spark.Spark.*;

public class SparkRestServer {
    private final int port;

    public SparkRestServer(int port) {
        this.port = port;
    }

    public void startServer() {
        // Set the server port
        port(port);

        // Define a GET endpoint at /hello
        get("/hello", (request, response) -> {
            response.type("text/plain");
            return "Hello, Spark Java!";
        });

        // Define a POST endpoint at /data to receive JSON
        post("/data", (request, response) -> {
            String contentType = request.contentType();
            String body = request.body();
            
            if (contentType != null && contentType.contains("application/xml")) {
                // Process XML payload
                // For demonstration, we'll simply echo it back in an XML response.
                response.type("application/xml");
                return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                       "<response>" +
                       "  <status>Received</status>" +
                       "  <data>" + body + "</data>" +
                       "</response>";
            } else if (contentType != null && contentType.contains("application/json")) {
                // Process JSON payload
                response.type("application/json");
                return "{\"status\":\"Received\",\"data\":" + body + "}";
            } else {
                // Unsupported Content-Type
                response.status(415); // Unsupported Media Type
                return "Unsupported Content-Type. Please send JSON or XML.";
            }
        });

        // Additional endpoint example
        get("/goodbye", (request, response) -> {
            response.type("text/plain");
            return "Goodbye from Spark Java!";
        });

        System.out.println("Spark REST server started on port " + port);
    }

    public void stopServer() {
        // Stop the Spark server (this is a static method from Spark)
        stop();
        System.out.println("Spark REST server stopped.");
    }
}