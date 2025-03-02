package com.example.client.model;

import java.util.List;

public class Capabilities {
    private String localDomain;
    private String localClassification;
    private List<Domain> reachableDomains;

    // No-argument constructor
    public Capabilities() {
    }

    // Parameterized constructor
    public Capabilities(String localDomain, String localClassification, List<Domain> reachableDomains) {
        this.localDomain = localDomain;
        this.localClassification = localClassification;
        this.reachableDomains = reachableDomains;
    }

    // Getter and setter for localDomain
    public String getLocalDomain() {
        return localDomain;
    }

    public void setLocalDomain(String localDomain) {
        this.localDomain = localDomain;
    }

    // Getter and setter for localClassification
    public String getLocalClassification() {
        return localClassification;
    }

    public void setLocalClassification(String localClassification) {
        this.localClassification = localClassification;
    }

    // Getter and setter for reachableDomains
    public List<Domain> getReachableDomains() {
        return reachableDomains;
    }

    public void setReachableDomains(List<Domain> reachableDomains) {
        this.reachableDomains = reachableDomains;
    }

    @Override
    public String toString() {
        return "Capabilities{" +
                "localDomain='" + localDomain + '\'' +
                ", localClassification='" + localClassification + '\'' +
                ", reachableDomains=" + reachableDomains +
                '}';
    }
}
