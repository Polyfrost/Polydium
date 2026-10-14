package org.polyfrost.polydium.client.perf;

//? if >= 26.2 {
import com.mojang.renderpearl.backend.vulkan.Destroyable;
import com.mojang.renderpearl.backend.vulkan.DestructionQueue;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTexture;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class BackgroundDestroyer implements DestructionQueue.Destroyer<Destroyable> {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "Polydium GPU destroyer");
        thread.setDaemon(true);
        return thread;
    });
    private static volatile CompletableFuture<Void> pending = CompletableFuture.completedFuture(null);

    private final DestructionQueue.Destroyer<Destroyable> vanilla;
    private List<Destroyable> batch;

    public BackgroundDestroyer(DestructionQueue.Destroyer<Destroyable> vanilla) {
        this.vanilla = vanilla;
    }

    @Override
    public void begin(int count) {
        if (pending.isCompletedExceptionally()) {
            pending.join();
        }
        this.vanilla.begin(count);
    }

    @Override
    public void destroy(Destroyable destroyable) {
        if ((destroyable instanceof VulkanGpuBuffer.Direct || destroyable instanceof VulkanGpuTexture || destroyable instanceof VulkanGpuTextureView)) {
            if (this.batch == null) {
                this.batch = new ArrayList<>();
            }
            this.batch.add(destroyable);
            return;
        }
        this.vanilla.destroy(destroyable);
    }

    @Override
    public void end() {
        this.vanilla.end();
        List<Destroyable> destroyables = this.batch;
        if (destroyables != null) {
            this.batch = null;
            DestructionQueue.Destroyer<Destroyable> vanilla = this.vanilla;
            pending = pending.thenRunAsync(() -> destroyables.forEach(vanilla::destroy), EXECUTOR);
        }
    }

    public static void drain() {
        pending.join();
    }
}
//?}
