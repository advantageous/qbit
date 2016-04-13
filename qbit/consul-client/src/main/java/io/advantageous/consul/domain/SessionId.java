package io.advantageous.consul.domain;

import io.advantageous.boon.json.annotations.JsonProperty;

public class SessionId {

    @JsonProperty("ID")
    private final String id;

    public SessionId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
