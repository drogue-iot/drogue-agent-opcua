package io.drogue.agent.opcua;

import static io.drogue.agent.opcua.Constants.STATE_CHANNEL;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import io.drogue.agent.opcua.state.State;
import io.drogue.agent.opcua.uplink.Uplink;

@ApplicationScoped
public class Sender {

    @Inject
    Uplink uplink;

    @Incoming(STATE_CHANNEL)
    public void sendState(final State state) {
        uplink.publish(state);
    }

}
