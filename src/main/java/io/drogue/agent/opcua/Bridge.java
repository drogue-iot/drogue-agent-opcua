package io.drogue.agent.opcua;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.drogue.agent.opcua.config.Configuration;
import io.drogue.agent.opcua.state.Item;
import io.drogue.agent.opcua.state.State;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets = {
        DataValue.class,
        NodeId.class,
})
public class Bridge {

    private static final Logger LOG = LoggerFactory.getLogger(Bridge.class);

    public enum ConnectionState {
        CONNECTED,
        DISCONNECTED,
    }

    public static class ItemState {
        private final DataValue value;

        ItemState(DataValue value) {
            this.value = value;
        }

        ItemState() {
            this(StatusCode.UNCERTAIN);
        }

        ItemState(StatusCode statusCode) {
            this.value = DataValue.newValue()
                    .setStatus(statusCode)
                    .build();
        }

        public DataValue getValue() {
            return value;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", ItemState.class.getSimpleName() + "[", "]")
                    .add("value=" + value)
                    .toString();
        }
    }

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final Configuration config;
    private OpcUaClient client;
    private Map<ReadValueId, ItemState> items;

    private volatile ConnectionState state;
    private volatile boolean running = false;

    public Bridge(Configuration config) {
        this.config = config;

        // create initial item state
        setState(ConnectionState.DISCONNECTED);
    }

    public State getState() {
        var items =
                this.items.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey(Comparator.comparing(ReadValueId::toString)))
                        .map(e ->
                                new Item(e.getKey(), e.getValue().getValue())
                        )
                        .collect(Collectors.toList());
        return new State(this.state, items);
    }

    public void start() throws UaException {
        this.running = true;
        this.scheduler.execute(this::reconnect);
    }

    public void stop() throws Exception {
        this.running = false;
        if (this.client != null) {
            this.client.disconnect().whenComplete(this::disconnectDone);
            this.client = null;
        }
    }

    void connectDone(UaClient client, Throwable error) {
        if (client == null || error != null) {
            // handle error
            setState(ConnectionState.DISCONNECTED);
        } else {
            // handle ok
            client.getSubscriptionManager().createSubscription(config.publishInterval.toMillis())
                    .whenComplete(this::createSubscriptionDone);
        }
    }

    private void createSubscriptionDone(UaSubscription subscription, Throwable throwable) {

        List<MonitoredItemCreateRequest> requests = new ArrayList<>(config.items.length);

        for (NodeId item : config.items) {

            ReadValueId readValueId = new ReadValueId(
                    item,
                    AttributeId.Value.uid(),
                    null,
                    QualifiedName.NULL_VALUE
            );

            var clientHandle = subscription.nextClientHandle();

            MonitoringParameters parameters = new MonitoringParameters(
                    clientHandle,
                    1000.0,     // sampling interval
                    null,       // filter, null means use default
                    uint(10),   // queue size
                    true        // discard oldest
            );

            MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(
                    readValueId,
                    MonitoringMode.Reporting,
                    parameters
            );

            requests.add(request);

        }

        UaSubscription.ItemCreationCallback onItemCreated =
                (item, id) -> item.setValueConsumer(this::onSubscriptionValue);

        subscription.createMonitoredItems(
                TimestampsToReturn.Both,
                requests,
                onItemCreated
        ).whenComplete(this::subscriptionDone);

    }

    private void subscriptionDone(List<UaMonitoredItem> items, Throwable throwable) {
        if (items == null || throwable != null) {
            setState(ConnectionState.DISCONNECTED);
            this.client.disconnect().whenComplete(this::disconnectDone);
        } else {
            for (UaMonitoredItem item : items) {
                if (!item.getStatusCode().isGood()) {
                    this.items.put(item.getReadValueId(), new ItemState(item.getStatusCode()));
                }
            }
            setState(ConnectionState.CONNECTED);
        }
    }

    private void disconnectDone(OpcUaClient opcUaClient, Throwable throwable) {
        this.client = null;
        setState(ConnectionState.DISCONNECTED);
    }

    private void onSubscriptionValue(UaMonitoredItem item, DataValue value) {
        LOG.info("onSubscriptionValue - item: {}, value: {}", item.getReadValueId(), value);
        this.items.put(item.getReadValueId(), new ItemState(value));
    }

    private void setState(ConnectionState state) {
        LOG.info("Change state - {} -> {}", this.state, state);
        this.state = state;
        switch (this.state) {
            case DISCONNECTED:
                this.items = new HashMap<>();
                for (NodeId item : config.items) {
                    ReadValueId readValueId = new ReadValueId(
                            item,
                            AttributeId.Value.uid(),
                            null,
                            QualifiedName.NULL_VALUE
                    );

                    this.items.put(readValueId, new ItemState());
                }
                // schedule re-connect
                if (this.running) {
                    scheduler.schedule(this::reconnect, 10, TimeUnit.SECONDS);
                }
                break;
            case CONNECTED:
                break;
        }
    }

    private void reconnect() {
        if (!this.running) {
            return;
        }

        try {
            this.client = OpcUaClient.create(
                    config.endpointUrl,
                    endpoints -> endpoints.stream().findFirst(),
                    configBuilder -> configBuilder
                            // metadata
                            .setApplicationName(LocalizedText.english(config.applicationName))
                            .setApplicationUri(config.applicationUri)
                            .setProductUri("https://drogue.io")
                            .setIdentityProvider(config.identityProvider)
                            // FIXME: add certificates
                            // timeout
                            .setRequestTimeout(uint(config.requestTimeout.toMillis()))
                            // build
                            .build());

            this.client.connect().whenComplete(this::connectDone);
        } catch (Exception e) {
            setState(ConnectionState.DISCONNECTED);
        }

    }
}
