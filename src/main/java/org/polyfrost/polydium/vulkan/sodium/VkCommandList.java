package org.polyfrost.polydium.vulkan.sodium;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.caffeinemc.mods.sodium.client.gl.array.GlVertexArray;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlBuffer;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlBufferMapFlags;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlBufferMapping;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlBufferStorageFlags;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlBufferTarget;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlBufferUsage;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlImmutableBuffer;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlMutableBuffer;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.gl.device.DrawCommandList;
import net.caffeinemc.mods.sodium.client.gl.sync.GlFence;
import net.caffeinemc.mods.sodium.client.gl.tessellation.GlPrimitiveType;
import net.caffeinemc.mods.sodium.client.gl.tessellation.GlTessellation;
import net.caffeinemc.mods.sodium.client.gl.tessellation.TessellationBinding;
import net.caffeinemc.mods.sodium.client.gl.util.EnumBitField;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public final class VkCommandList implements CommandList {
    public static final VkCommandList INSTANCE = new VkCommandList();

    private static final int USAGE = GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_SRC | GpuBuffer.USAGE_COPY_DST;

    public static GpuBuffer gpu(GlBuffer buffer) {
        return ((VkBacked) buffer).polydium$gpu();
    }

    private static CommandEncoder encoder() {
        return RenderSystem.getDevice().createCommandEncoder();
    }

    private static void release(GlBuffer buffer) {
        GpuBuffer old = gpu(buffer);
        if (old != null) {
            old.close();
            ((VkBacked) buffer).polydium$gpu(null);
        }
    }

    @Override
    public GlMutableBuffer createMutableBuffer() {
        return new GlMutableBuffer();
    }

    @Override
    public void allocateStorage(GlMutableBuffer buffer, long bufferSize, GlBufferUsage usage) {
        release(buffer);
        if (bufferSize > 0) {
            ((VkBacked) buffer).polydium$gpu(RenderSystem.getDevice().createBuffer(() -> "Sodium buffer", USAGE, bufferSize));
        }
        buffer.setSize(bufferSize);
    }

    @Override
    public void uploadData(GlMutableBuffer glBuffer, ByteBuffer byteBuffer, GlBufferUsage usage) {
        this.allocateStorage(glBuffer, byteBuffer.remaining(), usage);
        if (byteBuffer.hasRemaining()) {
            encoder().writeToBuffer(gpu(glBuffer).slice(), byteBuffer);
        }
    }

    @Override
    public void uploadDataToOffset(GlMutableBuffer buffer, int offset, long pointer, int size) {
        encoder().writeToBuffer(gpu(buffer).slice(offset, size), MemoryUtil.memByteBuffer(pointer, size));
    }

    @Override
    public void copyBufferSubData(GlBuffer src, GlBuffer dst, long readOffset, long writeOffset, long bytes) {
        encoder().copyToBuffer(gpu(src).slice(readOffset, bytes), gpu(dst).slice(writeOffset, bytes));
    }

    @Override
    public void deleteBuffer(GlBuffer buffer) {
        release(buffer);
        buffer.invalidateHandle();
    }

    @Override
    public GlBufferMapping mapBuffer(GlBuffer buffer, long offset, long length, EnumBitField<GlBufferMapFlags> flags) {
        if (offset != 0 || flags.contains(GlBufferMapFlags.READ)) {
            throw new UnsupportedOperationException("Only whole-buffer write mappings are supported on Vulkan");
        }
        GlBufferMapping mapping = new GlBufferMapping(buffer, MemoryUtil.memAlloc(Math.toIntExact(length)));
        buffer.setActiveMapping(mapping);
        return mapping;
    }

    @Override
    public void unmap(GlBufferMapping map) {
        ByteBuffer data = map.getMemoryBuffer();
        GlBuffer buffer = map.getBufferObject();
        try {
            encoder().writeToBuffer(gpu(buffer).slice(0, data.remaining()), data);
        } finally {
            MemoryUtil.memFree(data);
            buffer.setActiveMapping(null);
            map.dispose();
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void deleteTessellation(GlTessellation tessellation) {
    }

    @Override
    public GlImmutableBuffer createImmutableBuffer(long bufferSize, EnumBitField<GlBufferStorageFlags> flags) {
        throw glOnly();
    }

    @Override
    public GlTessellation createTessellation(GlPrimitiveType primitiveType, TessellationBinding[] bindings) {
        throw glOnly();
    }

    @Override
    public void bindVertexArray(GlVertexArray array) {
        throw glOnly();
    }

    @Override
    public void bindBuffer(GlBufferTarget target, GlBuffer buffer) {
        throw glOnly();
    }

    @Override
    public void unbindVertexArray() {
        throw glOnly();
    }

    @Override
    public void deleteVertexArray(GlVertexArray vertexArray) {
        throw glOnly();
    }

    @Override
    public DrawCommandList beginTessellating(GlTessellation tessellation) {
        throw glOnly();
    }

    @Override
    public void flushMappedRange(GlBufferMapping map, int offset, int length) {
        throw glOnly();
    }

    @Override
    public GlFence createFence() {
        throw glOnly();
    }

    private static UnsupportedOperationException glOnly() {
        return new UnsupportedOperationException("OpenGL-only Sodium device call reached on the Vulkan backend");
    }
}
