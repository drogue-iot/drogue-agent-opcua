package io.drogue.agent.opcua.uplink;

import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

@Path("/v1")
@RegisterRestClient(configKey = "drogue.cloud.uplink.client")
@RegisterClientHeaders(UplinkAuth.class)
public interface UplinkClient {

    @POST
    @Path("/{channel}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void publish(@PathParam String channel, @QueryParam Optional<String> dataSchema, Object payload);

}
