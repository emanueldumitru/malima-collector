package com.orange.malimacollector.entities.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Standalone {
    private boolean events; // /users/:id/events

    @JsonProperty("events")
    public boolean isEvents() {
        return events;
    }
}
