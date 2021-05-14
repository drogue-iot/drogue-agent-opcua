package io.drogue.agent.opcua.config;

import org.eclipse.microprofile.config.spi.Converter;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

public class NodeIdConverter implements Converter<NodeId> {
    @Override
    public NodeId convert(String value) {
        try {
            return NodeId.parse(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse NodeId", e);
        }
    }
}
