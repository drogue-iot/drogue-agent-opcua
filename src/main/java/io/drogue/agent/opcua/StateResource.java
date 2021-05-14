package io.drogue.agent.opcua;

import static io.drogue.agent.opcua.Constants.STATE_CHANNEL;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.reactive.RestSseElementType;

import io.drogue.agent.opcua.state.State;
import io.smallrye.mutiny.Multi;
import io.vertx.core.json.Json;

@Path("/state")
public class StateResource {
    @Inject
    BridgeBean bridge;

    @Inject
    @Channel(STATE_CHANNEL)
    Multi<State> stateChanges;

    @GET()
    @Path("/current")
    @Produces(MediaType.APPLICATION_JSON)
    public State current() {
        return bridge.getState();
    }

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestSseElementType(MediaType.APPLICATION_JSON)
    public Multi<String> stateChanges() {
        return Multi.createFrom()
                .item(bridge.getState())
                .onCompletion().switchTo(this.stateChanges)
                .map(Json::encode);
    }

}