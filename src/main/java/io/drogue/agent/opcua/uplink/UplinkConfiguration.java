package io.drogue.agent.opcua.uplink;

import java.util.Optional;

import io.quarkus.arc.config.ConfigProperties;

@ConfigProperties(prefix = "drogue.cloud.uplink", namingStrategy = ConfigProperties.NamingStrategy.VERBATIM)
public class UplinkConfiguration {
    public String application;
    public String device;
    public String password;
    public String channel = "default";

    public Optional<String> dataSchema;
}
