package com.orange.malimacollector.entities.JenkinsEntities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UnlabeledLoad {
    private String unlabeledLoadClass;

    @JsonProperty("_class")
    public String getUnlabeledLoadClass() { return unlabeledLoadClass; }
    @JsonProperty("_class")
    public void setUnlabeledLoadClass(String value) { this.unlabeledLoadClass = value; }
}

