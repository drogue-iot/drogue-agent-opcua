package io.drogue.agent.opcua.config;

import org.eclipse.microprofile.config.spi.Converter;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;

public class IdentityProviderConverter implements Converter<IdentityProvider> {
    @Override
    public IdentityProvider convert(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new AnonymousProvider();
        }

        if (value.startsWith("user:")) {
            value = value.substring(5);
            var toks = value.split(":", 2);
            if (toks.length == 2) {
                return new UsernameProvider(toks[0], toks[1]);
            }
        }

        throw new IllegalArgumentException(String.format("Unable to convert '%s' to an identity provider", value));

    }
}
