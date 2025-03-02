package com.example.client.model;

public class SubscriptionModel {
    private String topic;
    private String predicate; 

    // No-argument constructor (needed by many JSON libraries like Jackson)
    public SubscriptionModel() {
    }

    // Parameterized constructor
    public SubscriptionModel(String topic, String predicate) {
        this.topic = topic;
        this.predicate = predicate;
    }

    // Getter for messageType
    public String getTopic() {
        return topic;
    }

    // Setter for messageType
    public void setTopic(String topic) {
        this.topic = topic;
    }

    // Getter for payload
    public String getPredicate() {
        return predicate;
    }

    // Setter for payload
    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    // Overridden toString method for easy logging/debugging
    @Override
    public String toString() {
        return "SubscriptionMessage{" +
                "topic='" + topic + '\'' +
                ", predicate=" + predicate +
                '}';
    }
}
