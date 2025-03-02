package com.example.client.model; // Replace with your actual package name

public class CapabilitiesMessage {
    private String messageType;
    private Payload payload; // Ensure you have a corresponding Payload class

    // No-argument constructor (needed by many JSON libraries like Jackson)
    public CapabilitiesMessage() {
    }

    // Parameterized constructor
    public CapabilitiesMessage(String messageType, Payload payload) {
        this.messageType = messageType;
        this.payload = payload;
    }

    // Getter for messageType
    public String getMessageType() {
        return messageType;
    }

    // Setter for messageType
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    // Getter for payload
    public Payload getPayload() {
        return payload;
    }

    // Setter for payload
    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    // Overridden toString method for easy logging/debugging
    @Override
    public String toString() {
        return "CapabilitiesMessage{" +
                "messageType='" + messageType + '\'' +
                ", payload=" + payload +
                '}';
    }
}