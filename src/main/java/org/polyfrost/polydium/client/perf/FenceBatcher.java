package org.polyfrost.polydium.client.perf;

//? if >= 1.21.8 && < 26.2 {
/*import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Supplier;

public final class FenceBatcher {
    static Supplier<GpuFence> factory = () -> RenderSystem.getDevice().createCommandEncoder().createFence();

    private static Batch pending;

    public static GpuFence defer() {
        if (pending == null) {
            pending = new Batch();
        }
        pending.refs++;
        return new Handle(pending);
    }

    public static void flush() {
        Batch batch = pending;
        if (batch == null) {
            return;
        }
        pending = null;
        if (batch.refs > 0) {
            batch.fence = factory.get();
        }
    }

    private static final class Batch {
        GpuFence fence;
        int refs;
        boolean signaled;
    }

    private static final class Handle implements GpuFence {
        private Batch batch;

        Handle(Batch batch) {
            this.batch = batch;
        }

        @Override
        public boolean awaitCompletion(long timeout) {
            Batch b = batch;
            if (b.signaled) {
                return true;
            }
            if (b.fence == null) {
                flush();
            }
            return b.signaled = b.fence.awaitCompletion(timeout);
        }

        @Override
        public void close() {
            Batch b = batch;
            if (b == null) {
                return;
            }
            batch = null;
            if (--b.refs == 0 && b.fence != null) {
                b.fence.close();
            }
        }
    }
}
*///?}
