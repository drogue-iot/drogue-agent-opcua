package io.drogue.agent.opcua.uplink;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import io.quarkus.arc.config.ConfigProperties;

@ConfigProperties(prefix = "drogue.cloud.uplink", namingStrategy = ConfigProperties.NamingStrategy.VERBATIM)
public class UplinkConfiguration {
    public String url;
    public String application;
    public String device;
    public String password;
    public String channel = "default";

    public Optional<String> dataSchema;

    public String makeCredentials() {
        return makeUser() + ":" + this.password;
    }

    public String makeUser() {
        var app = URLEncoder.encode(this.application, StandardCharsets.UTF_8);
        var device = URLEncoder.encode(this.device, StandardCharsets.UTF_8);
        return device + "@" + app;
    }
}
