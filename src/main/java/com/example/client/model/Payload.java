package com.example.client.model;

public class Payload {
    private Capabilities capabilities;

    // No-argument constructor
    public Payload() {
    }

    // Parameterized constructor
    public Payload(Capabilities capabilities) {
        this.capabilities = capabilities;
    }

    // Getter for capabilities
    public Capabilities getCapabilities() {
        return capabilities;
    }

    // Setter for capabilities
    public void setCapabilities(Capabilities capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    public String toString() {
        return "Payload{" +
                "capabilities=" + capabilities +
                '}';
    }
}
