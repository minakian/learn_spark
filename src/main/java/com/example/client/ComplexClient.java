package com.example.client;

import com.example.client.connection.ConnectionManager;
import com.example.client.http.HttpClientService;
import com.example.client.message.MessageReceiver;
import com.example.client.ui.UserInteractionHandler;
import com.example.client.rest.SparkRestServer;
import com.example.client.store.ConfigStore;

import java.io.IOException;
import java.net.Socket;

public class ComplexClient {
    private static final int REST_SERVER_PORT = 4567;
    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 12345;
    private static final int RECONNECT_DELAY_MS = 1000;

    public static void main(String[] args) {
        ComplexClient client = new ComplexClient();
        client.start();
    }

    /**
     * Starts the application by launching the REST server, connecting to the server,
     * starting the message receiver, and entering the user interaction loop.
     */
    public void start() {
        // Start REST server on a separate thread
        new Thread(() -> {
            SparkRestServer restServer = new SparkRestServer(REST_SERVER_PORT);
            restServer.startServer();
        }).start();

        System.out.println(ConfigStore.getInstance().getSettings());

        while (true) {
            try {
                // Establish socket connection
                ConnectionManager connectionManager = new ConnectionManager();
                connectionManager.connect();

                // Start the receiver thread for incoming messages
                MessageReceiver receiver = new MessageReceiver(connectionManager);
                Thread receiverThread = new Thread(receiver);
                receiverThread.setDaemon(true);
                receiverThread.start();

                // Start user interaction loop
                UserInteractionHandler uiHandler = new UserInteractionHandler(connectionManager);
                uiHandler.run();

                // Cleanup on exit
                connectionManager.disconnect();
                break;
            } catch (IOException e) {
                System.out.println("Failed to connect to server. Retrying in " + RECONNECT_DELAY_MS / 1000 + " seconds...");
                try {
                    Thread.sleep(RECONNECT_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }
}