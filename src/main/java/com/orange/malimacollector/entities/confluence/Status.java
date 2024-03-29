package com.orange.malimacollector.entities.confluence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.IOException;

public enum Status {
    CURRENT;

    @JsonValue
    public String toValue() {
        if (this == Status.CURRENT) {
            return "current";
        }
        return null;
    }

    @JsonCreator
    public static Status forValue(String value) throws IOException {
        if (value.equals("current")) return CURRENT;
        throw new IOException("Cannot deserialize Status");
    }
}
