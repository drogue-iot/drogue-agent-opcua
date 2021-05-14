package io.drogue.agent.opcua.uplink;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

@ApplicationScoped
public class UplinkAuth implements ClientHeadersFactory {
    @Inject
    UplinkConfiguration configuration;

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> result = new MultivaluedMapImpl<>();

        var app = URLEncoder.encode(configuration.application, StandardCharsets.UTF_8);
        var device = URLEncoder.encode(configuration.device, StandardCharsets.UTF_8);
        var creds = String.format("%s@%s:%s", device, app, configuration.password);

        var auth = "Basic " +
                Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));

        result.add("Authorization", auth);

        return result;
    }
}
