package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPipelineCacheCreateInfo;
import org.polyfrost.polydium.client.perf.VulkanPipelineCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.file.Path;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Mixin(VulkanDevice.class)
public abstract class MixinVulkanPipelineCache implements VulkanPipelineCache {
    @Shadow
    @Final
    private VkDevice vkDevice;

    @Unique
    private long polydium$pipelineCache;
    @Unique
    private final ReadWriteLock polydium$pipelineCacheLock = new ReentrantReadWriteLock();

    @Unique
    private static Path polydium$pipelineCacheFile() {
        return FabricLoader.getInstance().getGameDir().resolve("polydium").resolve("pipelines.bin");
    }

    @Override
    public long polydium$pipelineCache() {
        return this.polydium$pipelineCache;
    }

    @Override
    public Lock polydium$pipelineCacheUse() {
        return this.polydium$pipelineCacheLock.readLock();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void polydium$createPipelineCache(CallbackInfo ci) {
        byte[] saved = VulkanPipelineCache.load(polydium$pipelineCacheFile());
        ByteBuffer initial = saved == null ? null : MemoryUtil.memAlloc(saved.length).put(0, saved);
        try {
            this.polydium$pipelineCache = this.polydium$newPipelineCache(initial);
            if (this.polydium$pipelineCache == 0L && initial != null) {
                this.polydium$pipelineCache = this.polydium$newPipelineCache(null);
            }
        } finally {
            MemoryUtil.memFree(initial);
        }
    }

    @Unique
    private long polydium$newPipelineCache(ByteBuffer initial) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer handle = stack.callocLong(1);
            VkPipelineCacheCreateInfo info = VkPipelineCacheCreateInfo.calloc(stack).sType$Default().pInitialData(initial);
            return VK10.vkCreatePipelineCache(this.vkDevice, info, null, handle) == VK10.VK_SUCCESS ? handle.get(0) : 0L;
        }
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkDestroyDevice(Lorg/lwjgl/vulkan/VkDevice;Lorg/lwjgl/vulkan/VkAllocationCallbacks;)V"))
    private void polydium$destroyPipelineCache(CallbackInfo ci) {
        Lock exclusive = this.polydium$pipelineCacheLock.writeLock();
        exclusive.lock();
        try {
            if (this.polydium$pipelineCache == 0L) {
                return;
            }
            this.polydium$savePipelineCache();
            VK10.vkDestroyPipelineCache(this.vkDevice, this.polydium$pipelineCache, null);
            this.polydium$pipelineCache = 0L;
        } finally {
            exclusive.unlock();
        }
    }

    @Unique
    private void polydium$savePipelineCache() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer size = stack.callocPointer(1);
            if (VK10.vkGetPipelineCacheData(this.vkDevice, this.polydium$pipelineCache, size, null) != VK10.VK_SUCCESS) {
                return;
            }
            ByteBuffer data = MemoryUtil.memAlloc((int) size.get(0));
            try {
                if (VK10.vkGetPipelineCacheData(this.vkDevice, this.polydium$pipelineCache, size, data) == VK10.VK_SUCCESS) {
                    byte[] bytes = new byte[(int) size.get(0)];
                    data.get(0, bytes);
                    VulkanPipelineCache.save(polydium$pipelineCacheFile(), bytes);
                }
            } finally {
                MemoryUtil.memFree(data);
            }
        }
    }
}
//?}
