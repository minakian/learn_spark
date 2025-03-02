package com.example.client;

import static spark.Spark.*;

import com.example.client.model.CapabilitiesMessage;
import com.example.client.model.SubscriptionModel;
import com.example.client.store.DataStore;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        post("/v1/api/capabilities", (req, res) -> {
            String jsonBody = req.body();
            ObjectMapper mapper = new ObjectMapper();
            CapabilitiesMessage message = mapper.readValue(jsonBody, CapabilitiesMessage.class);

            System.out.println("Received CapabilitiesMessage: " + message);
            
            // Store the parsed message in the shared DataStore
            DataStore.storeCapabilities(message);
            DataStore.setUpdated(true);
            
            res.status(200);
            return "Message stored successfully!";
        });

        post("/api/v1/subscribe", (req, res) -> {
            String jsonBody = req.body();
            ObjectMapper mapper = new ObjectMapper();

            SubscriptionModel msg = mapper.readValue(jsonBody, SubscriptionModel.class);
            
            System.out.println("New Message: \n" + jsonBody);
            res.status(200);
            return "New subscription";
        });

        System.out.println("Spark REST server started on port " + port);
    }

    public void stopServer() {
        // Stop the Spark server (this is a static method from Spark)
        stop();
        System.out.println("Spark REST server stopped.");
    }
}