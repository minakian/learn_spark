package com.example.client.model;

import java.util.List;

public class Domain {
    private String domain;
    private String classification;
    private List<CapabilityDetail> to;
    private List<CapabilityDetail> from;

    // No-argument constructor
    public Domain() {
    }

    // Parameterized constructor
    public Domain(String domain, String classification, List<CapabilityDetail> to, List<CapabilityDetail> from) {
        this.domain = domain;
        this.classification = classification;
        this.to = to;
        this.from = from;
    }

    // Getter and setter for domain
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    // Getter and setter for classification
    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    // Getter and setter for "to" list
    public List<CapabilityDetail> getTo() {
        return to;
    }

    public void setTo(List<CapabilityDetail> to) {
        this.to = to;
    }

    // Getter and setter for "from" list
    public List<CapabilityDetail> getFrom() {
        return from;
    }

    public void setFrom(List<CapabilityDetail> from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return "Domain{" +
                "domain='" + domain + '\'' +
                ", classification='" + classification + '\'' +
                ", to=" + to +
                ", from=" + from +
                '}';
    }
}
