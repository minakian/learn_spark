package com.example.client.ui;

import java.io.IOException;
import java.util.Scanner;

import com.example.client.connection.ConnectionManager;
import com.example.client.http.HttpClientService;
import com.example.client.message.MessageBuilder;
import afrl.ntf.broker_to_mms.v1.BrokerToMMSMessageOuterClass.BrokerToMMSMessage;
import afrl.ntf.broker_to_mms.v1.PathMetricInfoRequestOuterClass.PathMetricInfoRequest;
import com.example.client.store.DataStore;

public class UserInteractionHandler {
    private final ConnectionManager connectionManager;
    private static final int SLEEP_INTERVAL_MS = 1000;

    public UserInteractionHandler(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * Runs the command-line interface loop.
     */
    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            printMenu();
            while (running && connectionManager.isConnected()) {
                try {
                    // Optionally send a BrokerServiceMessage if new capabilities are available
                    if (DataStore.getUpdated()) {
                        BrokerToMMSMessage msg = MessageBuilder.buildBrokerServiceMessage();
                        if (msg != null) {
                            connectionManager.sendMessage(msg);
                            System.out.println("BrokerServiceMessage sent.");
                        }
                        DataStore.setUpdated(false);
                    }
                    if (scanner.hasNextLine()) {
                        String input = scanner.nextLine().trim();
                        switch (input) {
                            case "1":
                                BrokerToMMSMessage brokerMsg = MessageBuilder.buildBrokerServiceMessage();
                                if (brokerMsg != null) {
                                    connectionManager.sendMessage(brokerMsg);
                                    System.out.println("BrokerServiceMessage sent.");
                                }
                                break;
                            case "2":
                                PathMetricInfoRequest pathReq = MessageBuilder.buildPathMetricInfoRequest();
                                BrokerToMMSMessage msg = BrokerToMMSMessage.newBuilder()
                                        .setPathRequest(pathReq)
                                        .build();
                                connectionManager.sendMessage(msg);
                                System.out.println("PathMetricInfoRequest sent.");
                                break;
                            case "3":
                                running = false;
                                break;
                            case "4":
                                System.out.print("Enter GET endpoint URL: ");
                                String getEndpoint = scanner.nextLine().trim();
                                HttpClientService.sendGetRequest(getEndpoint);
                                break;
                            case "5":
                                System.out.print("Enter POST endpoint URL: ");
                                String postEndpoint = scanner.nextLine().trim();
                                System.out.print("Enter JSON payload: ");
                                String jsonPayload = scanner.nextLine().trim();
                                HttpClientService.sendPostRequest(postEndpoint, jsonPayload);
                                break;
                            default:
                                System.out.println("Invalid choice. Please try again.");
                                break;
                        }
                        if (running) {
                            printMenu();
                        }
                    } else {
                        Thread.sleep(SLEEP_INTERVAL_MS);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing command: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("User interaction interrupted: " + e.getMessage());
        }
    }

    private void printMenu() {
        System.out.println("\nClient Options:");
        System.out.println("1. Send BrokerServiceMessage");
        System.out.println("2. Send PathMetricInfoRequest");
        System.out.println("3. Exit");
        System.out.println("4. Send HTTP GET Request");
        System.out.println("5. Send HTTP POST Request");
        System.out.print("Enter choice: ");
    }
}
