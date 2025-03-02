package com.example.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AppSettings {
    private String name;
    private String platformName;
    private String domainName;
    @JsonProperty("platform_id")
    private Integer platformId;
    private String domain;
    @JsonProperty("ta2-endpoint")
    private String ta2Endpoint;

    public AppSettings(){}

    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
        parseName();
    }

    private void parseName() {
        // Assume the format is "P2-CBS-D1"
        if (name != null) {
            String[] parts = name.split("-");
            if (parts.length >= 3) {
                this.platformName = parts[0];
                this.domainName = parts[2];
            }
        }
    }

    public String getPlarformName() {
        return platformName;
    }

    public String getDomainName() {
        return domainName;
    }

    public Integer getPlatformId() {
        return platformId;
    }

    public void setPlatformId(Integer platformId) {
        this.platformId = platformId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getTa2Endpoint(){
        return ta2Endpoint;
    }

    public void setTa2Endpoint(String ta2Endpoint){
        this.ta2Endpoint = ta2Endpoint;
    }

    @Override
    public String toString() {
        return "AppSettings{" +
                "name='" + name + '\'' +
                ", platform_id=" + platformId +
                ", domain=" + domain +
                ", ta2-endpoint=" + ta2Endpoint +
                '}';
    }
}
