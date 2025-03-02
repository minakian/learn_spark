package com.example.client.store;

import com.example.client.model.CapabilitiesMessage;

public class DataStore {
    // Store only the latest CapabilitiesMessage.
    private static volatile CapabilitiesMessage latestMessage;
    private static volatile boolean updated;

    // Synchronized setter to store a new CapabilitiesMessage.
    public static synchronized void storeCapabilities(CapabilitiesMessage message) {
        latestMessage = message;
        System.out.println("Stored latest message: " + latestMessage);
    }

    public static synchronized void setUpdated(boolean status) {
        updated = status;
    }

    // Synchronized getter to retrieve the latest CapabilitiesMessage.
    public static synchronized CapabilitiesMessage getLatestCapabilitiesMessage() {
        return latestMessage;
    }

    public static synchronized boolean getUpdated() {
        return updated;
    }
}