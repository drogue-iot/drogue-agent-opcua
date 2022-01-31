package io.drogue.agent.opcua.uplink;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.drogue.agent.opcua.Bridge;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.OpenSSLEngineOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

@ApplicationScoped
public class Uplink {

    private static final Logger LOG = LoggerFactory.getLogger(Bridge.class);

    @Inject
    UplinkConfiguration configuration;

    @Inject
    Vertx vertx;

    WebClient web;

    @PostConstruct
    public void start() {

        var opts = new WebClientOptions()
                .setUseAlpn(false)
                .setOpenSslEngineOptions(new OpenSSLEngineOptions())
                .setKeepAlive(false);

        this.web = WebClient
                .create(vertx, opts);
    }

    @PreDestroy
    public void stop() {
        this.web.close();
    }

    public void publish(Object payload) {
        var url = String.format("%s/v1/%s", configuration.url, configuration.channel);
        LOG.info("Publishing using vertx: {}", url);
        web.postAbs(url)
                .basicAuthentication(Buffer.buffer(configuration.makeUser()), Buffer.buffer(configuration.password))
                .sendJson(payload, sent -> {
                    if (sent.succeeded()) {
                        LOG.info("Publishes -> {} {}", sent.result().statusCode(), sent.result().statusMessage());
                    } else {
                        LOG.info("Published", sent.cause());
                    }
                });
    }

}
