package com.networknt.kafka.entity;

import org.apache.kafka.common.serialization.Serde;

import java.util.List;

public class StreamsDLQMetadata {

    private Serde serde;
    private List<String> parentNames;

    public Serde getSerde() {
        return serde;
    }

    public void setSerde(Serde serde) {
        this.serde = serde;
    }

    public List<String> getParentNames() {
        return parentNames;
    }

    public void setParentNames(List<String> parentNames) {
        this.parentNames = parentNames;
    }


}
