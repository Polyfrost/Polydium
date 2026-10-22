package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
//? if >= 26.2 {
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass.RenderArea;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanDebug;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPass;
import com.mojang.renderpearl.backend.vulkan.checkpoints.CheckpointExtension;
//?} else {
/*import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import org.polyfrost.polydium.vulkan.VulkanCommandEncoder;
import org.polyfrost.polydium.vulkan.VulkanDebug;
import org.polyfrost.polydium.vulkan.VulkanDevice;
import org.polyfrost.polydium.vulkan.VulkanGpuBuffer;
import org.polyfrost.polydium.vulkan.VulkanRenderPass;
import org.polyfrost.polydium.vulkan.checkpoints.CheckpointExtension;
import org.polyfrost.polydium.vulkan.compat.RenderArea;
import org.polyfrost.polydium.vulkan.compat.RenderPassDescriptor;
*///?}
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearAttachment;
import org.lwjgl.vulkan.VkClearRect;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;

public final class VulkanDeferral {
    private final Host host;
    private final LongArrayList pendingRanges = new LongArrayList();
    private int deferDepth;
    private GpuTexture clearColorTexture;
    private Vector4fc clearColor;
    private GpuTexture clearDepthTexture;
    private double clearDepth;
    private boolean barrierPending;
    private long uploadBuffer;
    private long uploadStart;
    private long uploadEnd;
    private long readBuffer;
    private long readStart;
    private long readEnd;
    private VulkanRenderPass suspendedPass;
    private RenderPassDescriptor suspendedDescriptor;
    private RenderPassDescriptor openDescriptor;

    public interface Host {
        VkCommandBuffer polydium$currentCommandBuffer();

        VulkanRenderPass polydium$currentRenderPass();

        void polydium$setCurrentRenderPass(VulkanRenderPass pass);

        VulkanDevice polydium$device();

        CheckpointExtension.CheckpointStorage polydium$checkpointStorage();

        void polydium$clearColorUnsynced(MemoryStack stack, GpuTexture colorTexture, Vector4fc clearColor);

        void polydium$clearDepthUnsynced(MemoryStack stack, GpuTexture depthTexture, double clearDepth);

        void polydium$memoryBarrier(MemoryStack stack);
    }

    public VulkanDeferral(Host host) {
        this.host = host;
    }

    public void flushBeforeCommand() {
        this.endSuspendedPass();
        if (this.deferDepth == 0) {
            this.flushClears();
            this.flushBarrier();
        }
    }

    public void flushBeforeEnd() {
        this.endSuspendedPass();
        this.flushClears();
        this.flushBarrier();
    }

    public void deferDepthClear(GpuTexture depthTexture, double clearDepth) {
        if (this.clearColorTexture != null || this.clearDepthTexture != null) {
            this.flushClears();
        }
        this.clearDepthTexture = depthTexture;
        this.clearDepth = clearDepth;
    }

    public void deferColorClear(GpuTexture colorTexture, Vector4fc clearColor) {
        this.flushClears();
        this.clearColorTexture = colorTexture;
        this.clearColor = clearColor;
    }

    public void deferPairedDepthClear(GpuTexture depthTexture, double clearDepth) {
        this.clearDepthTexture = depthTexture;
        this.clearDepth = clearDepth;
    }

    public RenderPassDescriptor clearOnLoad(RenderPassDescriptor descriptor) {
        if (this.clearColorTexture == null && this.clearDepthTexture == null) {
            return descriptor;
        }
        if (this.canContinue(descriptor)) {
            return descriptor;
        }

        RenderArea area = area(descriptor);
        List<RenderPassDescriptor.Attachment<Optional<Vector4fc>>> colors = new ArrayList<>(descriptor.colorAttachments());
        RenderPassDescriptor.Attachment<OptionalDouble> depth = descriptor.depthAttachment();
        for (int index = 0; index < colors.size(); index++) {
            RenderPassDescriptor.Attachment<Optional<Vector4fc>> color = colors.get(index);
            if (color == null || this.clearColorTexture == null || !coversTexture(color.textureView(), this.clearColorTexture, area)) {
                continue;
            }
            if (color.clearValue().isEmpty()) {
                colors.set(index, new RenderPassDescriptor.Attachment<>(color.textureView(), Optional.of(this.clearColor)));
            }
            this.clearColorTexture = null;
        }
        if (depth != null && this.clearDepthTexture != null && coversTexture(depth.textureView(), this.clearDepthTexture, area)) {
            if (depth.clearValue().isEmpty()) {
                depth = new RenderPassDescriptor.Attachment<>(depth.textureView(), OptionalDouble.of(this.clearDepth));
            }
            this.clearDepthTexture = null;
        }
        this.flushClears();
        //? if >= 26.3 {
        return new RenderPassDescriptor(descriptor.label(), colors, depth, area);
        //?} else {
        /*RenderPassDescriptor replaced = RenderPassDescriptor.create(descriptor.label());
        replaced.colorAttachments = colors;
        replaced.depthAttachment = depth;
        replaced.renderArea = area;
        return replaced;
        *///?}
    }

    public VulkanRenderPass continueSuspendedPass(RenderPassDescriptor descriptor) {
        if (!this.canContinue(descriptor)) {
            if (RenderPassStats.ENABLED && this.suspendedPass != null) {
                RenderPassStats.refused(this.suspendedPass.getLabel(), descriptor.label(), this.barrierPending ? "upload barrier pending" : "attachments, area or clear differ");
            }
            return null;
        }
        if (RenderPassStats.ENABLED) {
            RenderPassStats.merged();
        }

        OptionalDouble depthClear = descriptor.depthAttachment() == null ? OptionalDouble.empty() : descriptor.depthAttachment().clearValue();
        if (this.clearDepthTexture != null) {
            if (depthClear.isEmpty()) {
                depthClear = OptionalDouble.of(this.clearDepth);
            }
            this.clearDepthTexture = null;
        }

        VkCommandBuffer commandBuffer = this.host.polydium$currentCommandBuffer();
        CheckpointExtension.CheckpointStorage checkpointStorage = this.host.polydium$checkpointStorage();
        VulkanDevice device = this.host.polydium$device();
        Supplier<String> previousLabel = this.suspendedPass.getLabel();
        VulkanDebug debug = device.instance().debug();
        debug.endDebugGroup(commandBuffer);
        checkpointStorage.recordCheckpoint(commandBuffer, CheckpointExtension.CheckpointType.END_RENDER_PASS, previousLabel);
        debug.beginDebugGroup(commandBuffer, descriptor.label());
        checkpointStorage.recordCheckpoint(commandBuffer, CheckpointExtension.CheckpointType.BEGIN_RENDER_PASS, descriptor.label());
        this.suspendedPass = null;
        this.suspendedDescriptor = null;
        this.openDescriptor = descriptor;
        VulkanRenderPass pass = new VulkanRenderPass(device, (VulkanCommandEncoder) (Object) this.host, commandBuffer, checkpointStorage, area(descriptor), outputWidth(descriptor), outputHeight(descriptor), descriptor.depthAttachment() != null, descriptor.label());
        this.host.polydium$setCurrentRenderPass(pass);
        if (depthClear.isPresent()) {
            clearDepthInPass(commandBuffer, area(descriptor), (float) depthClear.getAsDouble());
        }
        return pass;
    }

    public void rememberDescriptor(RenderPassDescriptor descriptor) {
        this.openDescriptor = descriptor;
        if (RenderPassStats.ENABLED) {
            RenderPassStats.begun();
        }
    }

    public void submitted() {
        if (RenderPassStats.ENABLED) {
            RenderPassStats.submit();
        }
    }

    public boolean suspendPass() {
        VulkanRenderPass current = this.host.polydium$currentRenderPass();
        if (current == null || this.openDescriptor == null) {
            return false;
        }
        if (this.suspendedPass != null) {
            throw new IllegalStateException("A render pass is already suspended");
        }
        this.suspendedPass = current;
        this.suspendedDescriptor = this.openDescriptor;
        this.openDescriptor = null;
        this.host.polydium$setCurrentRenderPass(null);
        return true;
    }

    public void beginUpload(GpuBufferSlice destination, ByteBuffer data) {
        this.deferDepth++;
        long buffer = ((VulkanGpuBuffer) destination.buffer()).vkBuffer();
        long start = destination.offset();
        long end = start + data.remaining();
        if (this.overlapsPending(buffer, start, end)) {
            this.flushBarrier();
        }
        this.uploadBuffer = buffer;
        this.uploadStart = start;
        this.uploadEnd = end;
        this.readBuffer = 0L;
    }

    public void beginCopy(GpuBufferSlice source, GpuBufferSlice target) {
        this.deferDepth++;
        long sourceBuffer = ((VulkanGpuBuffer) source.buffer()).vkBuffer();
        long targetBuffer = ((VulkanGpuBuffer) target.buffer()).vkBuffer();
        long sourceStart = source.offset();
        long sourceEnd = sourceStart + source.length();
        long targetStart = target.offset();
        long targetEnd = targetStart + source.length();
        if (this.overlapsPending(sourceBuffer, sourceStart, sourceEnd) || this.overlapsPending(targetBuffer, targetStart, targetEnd)) {
            this.flushBarrier();
        }
        this.uploadBuffer = targetBuffer;
        this.uploadStart = targetStart;
        this.uploadEnd = targetEnd;
        this.readBuffer = sourceBuffer;
        this.readStart = sourceStart;
        this.readEnd = sourceEnd;
    }

    public void beginTimestamp() {
        this.deferDepth++;
    }

    public void endDeferred() {
        this.deferDepth--;
    }

    public void deferUploadBarrier() {
        this.pendingRanges.add(this.uploadBuffer);
        this.pendingRanges.add(this.uploadStart);
        this.pendingRanges.add(this.uploadEnd);
        if (this.readBuffer != 0L) {
            this.pendingRanges.add(this.readBuffer);
            this.pendingRanges.add(this.readStart);
            this.pendingRanges.add(this.readEnd);
        }
        this.barrierPending = true;
    }

    private boolean canContinue(RenderPassDescriptor descriptor) {
        RenderPassDescriptor previous = this.suspendedDescriptor;
        if (this.suspendedPass == null || this.barrierPending) {
            return false;
        }
        if (this.clearColorTexture != null) {
            return false;
        }
        if (!area(descriptor).equals(area(previous)) || descriptor.colorAttachments().size() != previous.colorAttachments().size()) {
            return false;
        }
        for (int index = 0; index < descriptor.colorAttachments().size(); index++) {
            RenderPassDescriptor.Attachment<Optional<Vector4fc>> color = descriptor.colorAttachments().get(index);
            RenderPassDescriptor.Attachment<Optional<Vector4fc>> previousColor = previous.colorAttachments().get(index);
            if (color == null || previousColor == null) {
                if (color != previousColor) {
                    return false;
                }
                continue;
            }
            if (color.textureView() != previousColor.textureView() || color.clearValue().isPresent()) {
                return false;
            }
        }
        RenderPassDescriptor.Attachment<OptionalDouble> depth = descriptor.depthAttachment();
        RenderPassDescriptor.Attachment<OptionalDouble> previousDepth = previous.depthAttachment();
        if (depth == null || previousDepth == null) {
            return depth == previousDepth && this.clearDepthTexture == null;
        }
        if (depth.textureView() != previousDepth.textureView()) {
            return false;
        }
        return this.clearDepthTexture == null || coversTexture(depth.textureView(), this.clearDepthTexture, area(descriptor));
    }

    private static void clearDepthInPass(VkCommandBuffer commandBuffer, RenderArea area, float depth) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkClearRect.Buffer rects = VkClearRect.calloc(1, stack);
            rects.baseArrayLayer(0);
            rects.layerCount(1);
            rects.rect().offset().set(area.x(), area.y());
            rects.rect().extent().set(area.width(), area.height());
            VkClearAttachment.Buffer attachments = VkClearAttachment.calloc(1, stack);
            attachments.aspectMask(VK10.VK_IMAGE_ASPECT_DEPTH_BIT);
            attachments.clearValue().depthStencil().depth(depth);
            VK10.vkCmdClearAttachments(commandBuffer, attachments, rects);
        }
    }

    private void endSuspendedPass() {
        VulkanRenderPass pass = this.suspendedPass;
        if (pass == null) {
            return;
        }
        if (RenderPassStats.ENABLED) {
            RenderPassStats.split(pass.getLabel());
        }
        VkCommandBuffer commandBuffer = this.host.polydium$currentCommandBuffer();
        this.suspendedPass = null;
        this.suspendedDescriptor = null;
        //? if >= 26.2 {
        VulkanLegacyRendering.endRendering(this.host.polydium$device(), commandBuffer);
        //?} else {
        /*VulkanCommandEncoder.endRendering(this.host.polydium$device(), commandBuffer);
        *///?}
        this.host.polydium$device().instance().debug().endDebugGroup(commandBuffer);
        this.host.polydium$checkpointStorage().recordCheckpoint(commandBuffer, CheckpointExtension.CheckpointType.END_RENDER_PASS, pass.getLabel());
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VulkanCommandEncoder.memoryBarrier(commandBuffer, stack);
        }
    }

    private void flushClears() {
        this.endSuspendedPass();
        GpuTexture color = this.clearColorTexture;
        GpuTexture depth = this.clearDepthTexture;
        if (color == null && depth == null) {
            return;
        }
        this.clearColorTexture = null;
        this.clearDepthTexture = null;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            if (color != null) {
                this.host.polydium$clearColorUnsynced(stack, color, this.clearColor);
            }
            if (depth != null) {
                this.host.polydium$clearDepthUnsynced(stack, depth, this.clearDepth);
            }
            this.host.polydium$memoryBarrier(stack);
        }
    }

    private void flushBarrier() {
        this.endSuspendedPass();
        if (!this.barrierPending) {
            return;
        }
        VkCommandBuffer commandBuffer = this.host.polydium$currentCommandBuffer();
        if (commandBuffer == null) {
            throw new IllegalStateException("Upload barrier pending without a recording command buffer");
        }
        this.barrierPending = false;
        this.pendingRanges.clear();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VulkanCommandEncoder.memoryBarrier(commandBuffer, stack);
        }
    }

    private boolean overlapsPending(long buffer, long start, long end) {
        LongArrayList ranges = this.pendingRanges;
        for (int index = 0; index < ranges.size(); index += 3) {
            if (ranges.getLong(index) == buffer && ranges.getLong(index + 1) < end && start < ranges.getLong(index + 2)) {
                return true;
            }
        }
        return false;
    }

    private static RenderArea area(RenderPassDescriptor descriptor) {
        //? if >= 26.3 {
        return descriptor.renderArea();
        //?} else {
        /*return descriptor.renderArea;
        *///?}
    }

    static int outputWidth(RenderPassDescriptor descriptor) {
        int width = 0;
        for (RenderPassDescriptor.Attachment<Optional<Vector4fc>> color : descriptor.colorAttachments()) {
            if (color != null) {
                width = color.textureView().getWidth(0);
            }
        }
        if (descriptor.colorAttachments().isEmpty() && descriptor.depthAttachment() != null) {
            width = descriptor.depthAttachment().textureView().getWidth(0);
        }
        return width;
    }

    static int outputHeight(RenderPassDescriptor descriptor) {
        int height = 0;
        for (RenderPassDescriptor.Attachment<Optional<Vector4fc>> color : descriptor.colorAttachments()) {
            if (color != null) {
                height = color.textureView().getHeight(0);
            }
        }
        if (descriptor.colorAttachments().isEmpty() && descriptor.depthAttachment() != null) {
            height = descriptor.depthAttachment().textureView().getHeight(0);
        }
        return height;
    }

    private static boolean coversTexture(GpuTextureView view, GpuTexture texture, RenderArea area) {
        return view.texture() == texture && texture.getMipLevels() == 1 && view.baseMipLevel() == 0 && view.mipLevels() == 1 && area.fillsTexture(view);
    }
}
//?}
