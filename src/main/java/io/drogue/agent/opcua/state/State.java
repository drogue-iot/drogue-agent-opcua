package io.drogue.agent.opcua.state;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.drogue.agent.opcua.Bridge;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class State {
    private final Bridge.ConnectionState connection;
    private final List<Item> items;

    public State(final Bridge.ConnectionState connection, final List<Item> items) {
        this.connection = connection;
        this.items = items;
    }

    public Bridge.ConnectionState getConnection() {
        return this.connection;
    }

    public List<Item> getItems() {
        return this.items;
    }
}
