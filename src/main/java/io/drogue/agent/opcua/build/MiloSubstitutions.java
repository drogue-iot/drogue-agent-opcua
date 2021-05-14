package io.drogue.agent.opcua.build;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;

/***
 * This should fix some native compilation issues. But currently it doesn't.
 */
@TargetClass(org.eclipse.milo.opcua.stack.core.util.BufferUtil.class)
@Substitute
// FIXME: still fails
final class MiloSubstitutions {

    @Substitute
    public static CompositeByteBuf compositeBuffer() {
        return new CompositeByteBuf(ByteBufAllocator.DEFAULT, false, 16);
    }

    @Substitute
    public static ByteBuf pooledBuffer() {
        return Unpooled.buffer();
    }

    @Substitute
    public static ByteBuf pooledBuffer(int initialCapacity) {
        return Unpooled.buffer(initialCapacity);
    }

}