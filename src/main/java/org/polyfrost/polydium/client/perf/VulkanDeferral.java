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
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTexture;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
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
import org.polyfrost.polydium.vulkan.VulkanGpuTexture;
import org.polyfrost.polydium.vulkan.VulkanGpuTextureView;
import org.polyfrost.polydium.vulkan.VulkanRenderPass;
import org.polyfrost.polydium.vulkan.checkpoints.CheckpointExtension;
import org.polyfrost.polydium.vulkan.compat.RenderArea;
import org.polyfrost.polydium.vulkan.compat.RenderPassDescriptor;
*///?}
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.joml.Vector4fc;
import org.polyfrost.polydium.client.perf.gpuprofile.GpuProfileFlag;
import org.polyfrost.polydium.client.perf.gpuprofile.GpuProfiler;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRDynamicRendering;
import org.lwjgl.vulkan.KHRSynchronization2;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearAttachment;
import org.lwjgl.vulkan.VkClearRect;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDependencyInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier2;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderingAttachmentInfo;
import org.lwjgl.vulkan.VkRenderingInfo;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;

public final class VulkanDeferral {
    private final Host host;
    private final PendingRanges pendingRanges = new PendingRanges();
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
    private boolean openPassContinued;
    private boolean openPassSampledAttachment;
    private final Reference2IntOpenHashMap<GpuTexture> layouts = new Reference2IntOpenHashMap<>();

    public interface Host {
        VkCommandBuffer polydium$currentCommandBuffer();

        VulkanRenderPass polydium$currentRenderPass();

        void polydium$setCurrentRenderPass(VulkanRenderPass pass);

        VulkanDevice polydium$device();

        CheckpointExtension.CheckpointStorage polydium$checkpointStorage();

        void polydium$clearColorUnsynced(MemoryStack stack, GpuTexture colorTexture, Vector4fc clearColor);

        void polydium$clearDepthUnsynced(MemoryStack stack, GpuTexture depthTexture, double clearDepth);

        void polydium$memoryBarrier(MemoryStack stack);

        void polydium$sampled(GpuTextureView view);

        boolean polydium$dynamicRendering();

        void polydium$beginLegacyRenderPass(MemoryStack stack, RenderPassDescriptor descriptor, VkRect2D renderArea);

        default VkCommandBuffer polydium$commandBuffer() {
            return this.polydium$currentCommandBuffer();
        }

        default void polydium$general(GpuTexture texture) {
        }
    }

    public VulkanDeferral(Host host) {
        this.host = host;
        this.layouts.defaultReturnValue(VK10.VK_IMAGE_LAYOUT_GENERAL);
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
        if (GpuProfileFlag.ENABLED)
            GpuProfiler.discard();
        if (this.clearColorTexture != null || this.clearDepthTexture != null) {
            this.flushClears();
        }
        this.clearDepthTexture = depthTexture;
        this.clearDepth = clearDepth;
    }

    public void deferColorClear(GpuTexture colorTexture, Vector4fc clearColor) {
        if (GpuProfileFlag.ENABLED)
            GpuProfiler.discard();
        this.flushClears();
        this.clearColorTexture = colorTexture;
        this.clearColor = clearColor;
    }

    public void deferPairedDepthClear(GpuTexture depthTexture, double clearDepth) {
        if (GpuProfileFlag.ENABLED)
            GpuProfiler.discard();
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
        return replaced(descriptor, colors, depth, area);
    }

    private static RenderPassDescriptor replaced(RenderPassDescriptor descriptor, List<RenderPassDescriptor.Attachment<Optional<Vector4fc>>> colors, RenderPassDescriptor.Attachment<OptionalDouble> depth, RenderArea area) {
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
        this.openPassContinued = true;
        this.openPassSampledAttachment = false;
        VulkanRenderPass pass = new VulkanRenderPass(device, (VulkanCommandEncoder) (Object) this.host, commandBuffer, checkpointStorage, area(descriptor), outputWidth(descriptor), outputHeight(descriptor), descriptor.depthAttachment() != null, descriptor.label());
        this.host.polydium$setCurrentRenderPass(pass);
        if (GpuProfileFlag.ENABLED && GpuProfiler.recording) {
            GpuProfiler.passContinued();
        }
        if (depthClear.isPresent()) {
            clearDepthInPass(commandBuffer, area(descriptor), (float) depthClear.getAsDouble());
        }
        return pass;
    }

    public void rememberDescriptor(RenderPassDescriptor descriptor) {
        this.openDescriptor = descriptor;
        this.openPassContinued = false;
        this.openPassSampledAttachment = false;
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
        if (this.openPassSampledAttachment) {
            this.openDescriptor = null;
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

    public void sampled(GpuTextureView view) {
        RenderPassDescriptor descriptor = this.openDescriptor;
        if (descriptor == null) {
            return;
        }
        GpuTexture texture = view.texture();
        boolean attached = attaches(descriptor, texture);
        boolean general = this.layouts.getInt(texture) == VK10.VK_IMAGE_LAYOUT_GENERAL;
        if (attached) {
            this.openPassSampledAttachment = true;
        }
        if (!general || attached && this.openPassContinued) {
            this.openPassContinued = false;
            this.restartPass(descriptor, texture, attached);
        }
    }

    public void beginRendering(VkCommandBuffer commandBuffer, VkRenderingInfo renderingInfo, RenderPassDescriptor descriptor) {
        List<RenderPassDescriptor.Attachment<Optional<Vector4fc>>> colors = descriptor.colorAttachments();
        RenderPassDescriptor.Attachment<OptionalDouble> depth = descriptor.depthAttachment();
        RenderArea area = area(descriptor);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkImageMemoryBarrier2.Buffer barriers = VkImageMemoryBarrier2.calloc(colors.size() + 1, stack);
            for (int index = 0; index < colors.size(); index++) {
                RenderPassDescriptor.Attachment<Optional<Vector4fc>> color = colors.get(index);
                if (color != null) {
                    this.attach(barriers, renderingInfo.pColorAttachments().get(index), color.textureView(), area, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
                }
            }
            if (depth != null) {
                this.attach(barriers, renderingInfo.pDepthAttachment(), depth.textureView(), area, VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
            }
            if (barriers.flip().hasRemaining()) {
                KHRSynchronization2.vkCmdPipelineBarrier2KHR(commandBuffer, VkDependencyInfo.calloc(stack).sType$Default().pImageMemoryBarriers(barriers));
            }
        }
    }

    private void attach(VkImageMemoryBarrier2.Buffer barriers, VkRenderingAttachmentInfo info, GpuTextureView view, RenderArea area, int layout) {
        GpuTexture texture = view.texture();
        if (this.layouts.getInt(texture) == VK10.VK_IMAGE_LAYOUT_GENERAL) {
            if (this.layouts.containsKey(texture) || info.loadOp() != VK10.VK_ATTACHMENT_LOAD_OP_CLEAR || texture.getDepthOrLayers() != 1 || !coversTexture(view, texture, area) || hasStencil(texture)) {
                return;
            }
            this.layouts.keySet().removeIf(GpuTexture::isClosed);
            this.layouts.put(texture, layout);
            layoutBarrier(barriers.get(), texture, VK10.VK_IMAGE_LAYOUT_UNDEFINED, layout);
        }
        info.imageLayout(layout);
    }

    public void toGeneral(GpuTexture texture, boolean discard) {
        int current = this.layouts.getInt(texture);
        if (current == VK10.VK_IMAGE_LAYOUT_GENERAL) {
            return;
        }
        VkCommandBuffer commandBuffer = this.host.polydium$commandBuffer();
        this.layouts.removeInt(texture);
        transition(commandBuffer, texture, discard && !hasStencil(texture) ? VK10.VK_IMAGE_LAYOUT_UNDEFINED : current, VK10.VK_IMAGE_LAYOUT_GENERAL);
    }

    private static void transition(VkCommandBuffer commandBuffer, GpuTexture texture, int oldLayout, int newLayout) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkImageMemoryBarrier2.Buffer barriers = VkImageMemoryBarrier2.calloc(1, stack);
            layoutBarrier(barriers.get(0), texture, oldLayout, newLayout);
            KHRSynchronization2.vkCmdPipelineBarrier2KHR(commandBuffer, VkDependencyInfo.calloc(stack).sType$Default().pImageMemoryBarriers(barriers));
        }
    }

    private static void layoutBarrier(VkImageMemoryBarrier2 barrier, GpuTexture texture, int oldLayout, int newLayout) {
        int aspect = oldLayout == VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL || newLayout == VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL
            ? VK10.VK_IMAGE_ASPECT_COLOR_BIT
            : VK10.VK_IMAGE_ASPECT_DEPTH_BIT | (hasStencil(texture) ? VK10.VK_IMAGE_ASPECT_STENCIL_BIT : 0);
        barrier.sType$Default()
            .srcStageMask(KHRSynchronization2.VK_PIPELINE_STAGE_2_ALL_COMMANDS_BIT_KHR)
            .srcAccessMask(KHRSynchronization2.VK_ACCESS_2_MEMORY_READ_BIT_KHR | KHRSynchronization2.VK_ACCESS_2_MEMORY_WRITE_BIT_KHR)
            .dstStageMask(KHRSynchronization2.VK_PIPELINE_STAGE_2_ALL_COMMANDS_BIT_KHR)
            .dstAccessMask(KHRSynchronization2.VK_ACCESS_2_MEMORY_READ_BIT_KHR | KHRSynchronization2.VK_ACCESS_2_MEMORY_WRITE_BIT_KHR)
            .oldLayout(oldLayout)
            .newLayout(newLayout)
            .srcQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
            .dstQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
            .image(((VulkanGpuTexture) texture).vkImage());
        barrier.subresourceRange().aspectMask(aspect).baseMipLevel(0).levelCount(texture.getMipLevels()).baseArrayLayer(0).layerCount(texture.getDepthOrLayers());
    }

    private static boolean hasStencil(GpuTexture texture) {
        //? if >= 26.2 {
        return texture.getFormat().hasStencilAspect();
        //?} else {
        /*return false;
        *///?}
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
        this.pendingRanges.add(this.uploadBuffer, this.uploadStart, this.uploadEnd, this.readBuffer, this.readStart, this.readEnd);
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
            if (GpuProfileFlag.ENABLED)
                GpuProfiler.beginFlush(commandBuffer, GpuProfiler.OP_CLEAR);
            VK10.vkCmdClearAttachments(commandBuffer, attachments, rects);
            if (GpuProfileFlag.ENABLED)
                GpuProfiler.endFlush(commandBuffer);
        }
    }

    private void restartPass(RenderPassDescriptor descriptor, GpuTexture sampled, boolean pinGeneral) {
        if (RenderPassStats.ENABLED) {
            RenderPassStats.split(this.host.polydium$currentRenderPass().getLabel());
        }
        VkCommandBuffer commandBuffer = this.host.polydium$currentCommandBuffer();
        RenderArea area = area(descriptor);
        RenderPassDescriptor.Attachment<OptionalDouble> depth = descriptor.depthAttachment();
        if (depth != null) {
            depth = new RenderPassDescriptor.Attachment<>(depth.textureView(), OptionalDouble.empty());
        }
        RenderPassDescriptor loaded = replaced(descriptor, descriptor.colorAttachments(), depth, area);
        this.endRendering(commandBuffer);
        int current = this.layouts.getInt(sampled);
        if (current != VK10.VK_IMAGE_LAYOUT_GENERAL) {
            transition(commandBuffer, sampled, current, VK10.VK_IMAGE_LAYOUT_GENERAL);
            this.layouts.removeInt(sampled);
        }
        if (pinGeneral) {
            this.layouts.put(sampled, VK10.VK_IMAGE_LAYOUT_GENERAL);
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VulkanCommandEncoder.memoryBarrier(commandBuffer, stack);
            VkRect2D renderArea = VkRect2D.calloc(stack);
            renderArea.offset().set(area.x(), area.y());
            renderArea.extent().set(area.width(), area.height());
            if (this.host.polydium$dynamicRendering()) {
                this.beginLoadedRendering(stack, commandBuffer, loaded, renderArea);
            } else {
                this.host.polydium$beginLegacyRenderPass(stack, loaded, renderArea);
            }
        }
    }

    private void beginLoadedRendering(MemoryStack stack, VkCommandBuffer commandBuffer, RenderPassDescriptor descriptor, VkRect2D renderArea) {
        List<RenderPassDescriptor.Attachment<Optional<Vector4fc>>> colors = descriptor.colorAttachments();
        VkRenderingAttachmentInfo.Buffer colorInfos = VkRenderingAttachmentInfo.calloc(colors.size(), stack);
        for (int index = 0; index < colors.size(); index++) {
            RenderPassDescriptor.Attachment<Optional<Vector4fc>> color = colors.get(index);
            VkRenderingAttachmentInfo info = colorInfos.get(index).sType$Default();
            if (color == null) {
                info.imageLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED).loadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE).storeOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE);
            } else {
                this.loadAttachment(info, color.textureView());
            }
        }
        VkRenderingInfo renderingInfo = VkRenderingInfo.calloc(stack).sType$Default().renderArea(renderArea).layerCount(1).viewMask(0).pColorAttachments(colorInfos);
        if (descriptor.depthAttachment() != null) {
            renderingInfo.pDepthAttachment(this.loadAttachment(VkRenderingAttachmentInfo.calloc(stack).sType$Default(), descriptor.depthAttachment().textureView()));
        }
        KHRDynamicRendering.vkCmdBeginRenderingKHR(commandBuffer, renderingInfo);
    }

    private VkRenderingAttachmentInfo loadAttachment(VkRenderingAttachmentInfo info, GpuTextureView view) {
        return info.imageView(((VulkanGpuTextureView) view).vkImageView())
            .imageLayout(this.layouts.getInt(view.texture()))
            .loadOp(VK10.VK_ATTACHMENT_LOAD_OP_LOAD)
            .storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE);
    }

    private void endRendering(VkCommandBuffer commandBuffer) {
        //? if >= 26.2 {
        VulkanLegacyRendering.endRendering(this.host.polydium$device(), commandBuffer);
        //?} else {
        /*VulkanCommandEncoder.endRendering(this.host.polydium$device(), commandBuffer);
        *///?}
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
        this.endRendering(commandBuffer);
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
        VkCommandBuffer commandBuffer = this.host.polydium$currentCommandBuffer();
        if (GpuProfileFlag.ENABLED)
            GpuProfiler.beginFlush(commandBuffer, GpuProfiler.OP_CLEAR);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            if (color != null) {
                this.host.polydium$clearColorUnsynced(stack, color, this.clearColor);
            }
            if (depth != null) {
                this.host.polydium$clearDepthUnsynced(stack, depth, this.clearDepth);
            }
            this.host.polydium$memoryBarrier(stack);
        }
        if (GpuProfileFlag.ENABLED)
            GpuProfiler.endFlush(commandBuffer);
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
        return this.pendingRanges.overlaps(buffer, start, end);
    }

    private static RenderArea area(RenderPassDescriptor descriptor) {
        //? if >= 26.3 {
        return descriptor.renderArea();
        //?} else {
        /*return descriptor.renderArea;
        *///?}
    }

    public static int outputWidth(RenderPassDescriptor descriptor) {
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

    public static int outputHeight(RenderPassDescriptor descriptor) {
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

    private static boolean attaches(RenderPassDescriptor descriptor, GpuTexture texture) {
        for (RenderPassDescriptor.Attachment<Optional<Vector4fc>> color : descriptor.colorAttachments()) {
            if (color != null && color.textureView().texture() == texture) {
                return true;
            }
        }
        return descriptor.depthAttachment() != null && descriptor.depthAttachment().textureView().texture() == texture;
    }

    private static boolean coversTexture(GpuTextureView view, GpuTexture texture, RenderArea area) {
        return view.texture() == texture && texture.getMipLevels() == 1 && view.baseMipLevel() == 0 && view.mipLevels() == 1 && area.fillsTexture(view);
    }
}
//?}
