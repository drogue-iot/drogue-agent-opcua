package io.drogue.agent.opcua.config;

import java.time.Duration;

import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

import io.quarkus.arc.config.ConfigProperties;

@ConfigProperties(prefix = "drogue.agent.opcua", namingStrategy = ConfigProperties.NamingStrategy.VERBATIM)
public class Configuration {
    public String applicationName = "Drogue IoT OPC UA agent";
    public String applicationUri = "urn:drogue:iot:agent:opcua";

    public String endpointUrl;
    public SecurityPolicy securityPolicy = SecurityPolicy.Basic256Sha256;
    public IdentityProvider identityProvider = new AnonymousProvider();

    public Duration requestTimeout = Duration.ofSeconds(5);
    public Duration publishInterval = Duration.ofSeconds(1);

    public NodeId[] items;
}
