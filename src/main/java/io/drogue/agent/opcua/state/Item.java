package io.drogue.agent.opcua.state;

import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;

public class Item {
    private ReadValueId id;
    private DataValue value;

    public Item(final ReadValueId id, final DataValue value) {
        this.id = id;
        this.value = value;
    }

    public ReadValueId getId() {
        return this.id;
    }

    public DataValue getValue() {
        return this.value;
    }

}
