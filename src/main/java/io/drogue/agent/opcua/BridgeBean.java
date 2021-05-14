package io.drogue.agent.opcua;

import static io.drogue.agent.opcua.Constants.STATE_CHANNEL;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.milo.opcua.stack.core.UaException;

import io.drogue.agent.opcua.config.Configuration;
import io.drogue.agent.opcua.state.State;
import io.quarkus.runtime.Startup;
import io.reactivex.Flowable;
import io.smallrye.reactive.messaging.annotations.Broadcast;

@ApplicationScoped
@Startup
public class BridgeBean {
    @Inject
    Configuration config;

    Bridge bridge;

    @PostConstruct
    void start() throws UaException {
        this.bridge = new Bridge(config);
        this.bridge.start();
    }

    @PreDestroy
    void stop() throws Exception {
        if (this.bridge != null) {
            this.bridge.stop();
            this.bridge = null;
        }
    }

    public State getState() {
        return this.bridge.getState();
    }

    @Outgoing(STATE_CHANNEL)
    @Broadcast
    public Flowable<State> sendState() {
        return Flowable.interval(10, TimeUnit.SECONDS)
                .map(tick ->
                        getState()
                );
    }
}
