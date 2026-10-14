package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineCacheCreateInfo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.CRC32;

public final class VulkanPipelineCache {
    private static final int HEADER_SIZE = 32;
    private static final int CRC_SIZE = 4;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private long handle;

    public interface Holder {
        VulkanPipelineCache polydium$pipelineCache();
    }

    private static Path file() {
        return FabricLoader.getInstance().getGameDir().resolve("polydium").resolve("pipelines.bin");
    }

    public void create(VkDevice device) {
        byte[] saved = load(file());
        ByteBuffer initial = saved == null ? null : MemoryUtil.memAlloc(saved.length).put(0, saved);
        try {
            this.handle = newCache(device, initial);
            if (this.handle == 0L && initial != null) {
                this.handle = newCache(device, null);
            }
        } finally {
            MemoryUtil.memFree(initial);
        }
    }

    private static long newCache(VkDevice device, ByteBuffer initial) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer handle = stack.callocLong(1);
            VkPipelineCacheCreateInfo info = VkPipelineCacheCreateInfo.calloc(stack).sType$Default().pInitialData(initial);
            return VK10.vkCreatePipelineCache(device, info, null, handle) == VK10.VK_SUCCESS ? handle.get(0) : 0L;
        }
    }

    public int createPipelines(VkDevice device, VkGraphicsPipelineCreateInfo.Buffer createInfos, VkAllocationCallbacks allocator, LongBuffer pipelines, Operation<Integer> original) {
        Lock use = this.lock.readLock();
        use.lock();
        try {
            return original.call(device, this.handle, createInfos, allocator, pipelines);
        } finally {
            use.unlock();
        }
    }

    public void destroy(VkDevice device) {
        Lock exclusive = this.lock.writeLock();
        exclusive.lock();
        try {
            if (this.handle == 0L) {
                return;
            }
            this.save(device);
            VK10.vkDestroyPipelineCache(device, this.handle, null);
            this.handle = 0L;
        } finally {
            exclusive.unlock();
        }
    }

    private void save(VkDevice device) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer size = stack.callocPointer(1);
            if (VK10.vkGetPipelineCacheData(device, this.handle, size, null) != VK10.VK_SUCCESS) {
                return;
            }
            ByteBuffer data = MemoryUtil.memAlloc((int) size.get(0));
            try {
                if (VK10.vkGetPipelineCacheData(device, this.handle, size, data) == VK10.VK_SUCCESS) {
                    byte[] bytes = new byte[(int) size.get(0)];
                    data.get(0, bytes);
                    save(file(), bytes);
                }
            } finally {
                MemoryUtil.memFree(data);
            }
        }
    }

    private static boolean valid(byte[] blob) {
        if (blob.length <= HEADER_SIZE) {
            return false;
        }
        ByteBuffer header = ByteBuffer.wrap(blob).order(ByteOrder.nativeOrder());
        return header.getInt(0) == HEADER_SIZE && header.getInt(4) == 1;
    }

    private static byte[] load(Path file) {
        try {
            byte[] bytes = Files.readAllBytes(file);
            if (bytes.length <= CRC_SIZE || ByteBuffer.wrap(bytes).getInt(0) != crc(bytes)) {
                return null;
            }
            byte[] blob = Arrays.copyOfRange(bytes, CRC_SIZE, bytes.length);
            return valid(blob) ? blob : null;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    private static void save(Path file, byte[] blob) {
        if (!valid(blob)) {
            return;
        }
        try {
            byte[] bytes = new byte[CRC_SIZE + blob.length];
            System.arraycopy(blob, 0, bytes, CRC_SIZE, blob.length);
            ByteBuffer.wrap(bytes).putInt(0, crc(bytes));
            Path directory = file.toAbsolutePath().getParent();
            Files.createDirectories(directory);
            Path temporary = Files.createTempFile(directory, "pipelines", ".tmp");
            try {
                Files.write(temporary, bytes);
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temporary);
            }
        } catch (IOException | RuntimeException e) {
            LogUtils.getLogger().warn("Could not write pipeline cache file {}", file, e);
        }
    }

    private static int crc(byte[] bytes) {
        CRC32 crc = new CRC32();
        crc.update(bytes, CRC_SIZE, bytes.length - CRC_SIZE);
        return (int) crc.getValue();
    }
}
//?}
