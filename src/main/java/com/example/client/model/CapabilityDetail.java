package com.example.client.model;

public class CapabilityDetail {
    private String messageType;
    private int latency;

    // No-argument constructor
    public CapabilityDetail() {
    }

    // Parameterized constructor
    public CapabilityDetail(String messageType, int latency) {
        this.messageType = messageType;
        this.latency = latency;
    }

    // Getter and setter for messageType
    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    // Getter and setter for latency
    public int getLatency() {
        return latency;
    }

    public void setLatency(int latency) {
        this.latency = latency;
    }

    @Override
    public String toString() {
        return "CapabilityDetail{" +
                "messageType='" + messageType + '\'' +
                ", latency=" + latency +
                '}';
    }
}
