package com.example.client.model;

public class SubscriptionModel {
    private String topic;
    private String predicate;
    private int subscriberId;
    private String subscriberName;
    private String imsIdentifier;

    // No-argument constructor (needed by many JSON libraries like Jackson)
    public SubscriptionModel() {
    }

    // Parameterized constructor
    public SubscriptionModel(String topic, String predicate, int subscriberId, String subscriberName, String imsIdentifier) {
        this.topic = topic;
        this.predicate = predicate;
        this.subscriberId = subscriberId;
        this.subscriberName = subscriberName;
        this.imsIdentifier = imsIdentifier;
    }

    // Getter for topic
    public String getTopic() {
        return topic;
    }

    // Setter for topic
    public void setTopic(String topic) {
        this.topic = topic;
    }

    // Getter for predicate
    public String getPredicate() {
        return predicate;
    }

    // Setter for predicate
    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    // Getter for subscriberId
    public int getSubscriberId() {
        return subscriberId;
    }

    // Setter for subscriberId
    public void setSubscriberId(int subscriberId) {
        this.subscriberId = subscriberId;
    }

    // Getter for subscriberName
    public String getSubscriberName() {
        return subscriberName;
    }

    // Setter for subscriberName
    public void setSubscriberName(String subscriberName) {
        this.subscriberName = subscriberName;
    }

    // Getter for imsIdentifier
    public String getImsIdentifier() {
        return imsIdentifier;
    }

    // Setter for imsIdentifier
    public void setImsIdentifier(String imsIdentifier) {
        this.imsIdentifier = imsIdentifier;
    }

    // Overridden toString method for easy logging/debugging
    @Override
    public String toString() {
        return "SubscriptionMessage{" +
                "topic='" + topic + '\'' +
                ", predicate='" + predicate + '\'' +
                ", subscriberId=" + subscriberId +
                ", subscriberName='" + subscriberName + '\'' +
                ", imsIdentifier='" + imsIdentifier + '\'' +
                '}';
    }
}
