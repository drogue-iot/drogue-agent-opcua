package io.drogue.agent.opcua.uplink;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class Uplink {

    @Inject
    UplinkConfiguration configuration;

    @Inject
    @RestClient
    UplinkClient client;

    public void publish(Object payload) {
        client.publish(
                configuration.channel,
                configuration.dataSchema,
                payload
        );
    }

}
