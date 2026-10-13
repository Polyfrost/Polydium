package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanDebug;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPass;
import com.mojang.renderpearl.backend.vulkan.checkpoints.CheckpointExtension;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRDynamicRendering;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearAttachment;
import org.lwjgl.vulkan.VkClearRect;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.polyfrost.polydium.client.perf.RenderPassStats;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;

@Mixin(VulkanCommandEncoder.class)
public abstract class MixinVulkanDeferral {
    @Shadow
    private VkCommandBuffer currentCommandBuffer;

    @Shadow
    private VulkanRenderPass currentRenderPass;

    @Shadow
    @Final
    private VulkanDevice device;

    @Shadow
    @Final
    private CheckpointExtension.CheckpointStorage checkpointStorage;

    @Unique
    private final LongArrayList polydium$pendingRanges = new LongArrayList();

    @Unique
    private int polydium$deferDepth;

    @Unique
    private GpuTexture polydium$clearColorTexture;

    @Unique
    private Vector4fc polydium$clearColor;

    @Unique
    private GpuTexture polydium$clearDepthTexture;

    @Unique
    private double polydium$clearDepth;

    @Unique
    private boolean polydium$barrierPending;

    @Unique
    private long polydium$uploadBuffer;

    @Unique
    private long polydium$uploadStart;

    @Unique
    private long polydium$uploadEnd;

    @Unique
    private long polydium$readBuffer;

    @Unique
    private long polydium$readStart;

    @Unique
    private long polydium$readEnd;

    @Unique
    private VulkanRenderPass polydium$suspendedPass;

    @Unique
    private RenderPassDescriptor polydium$suspendedDescriptor;

    @Unique
    private RenderPassDescriptor polydium$openDescriptor;

    @Shadow
    public abstract void clearDepthTextureUnsynced(MemoryStack stack, GpuTexture depthTexture, double clearDepth);

    @Shadow
    private void clearColorTextureUnsynced(MemoryStack stack, GpuTexture colorTexture, Vector4fc clearColor) {
        throw new AssertionError();
    }

    @Shadow
    private void memoryBarrier(MemoryStack stack) {
        throw new AssertionError();
    }

    @Inject(method = "commandBuffer", at = @At("HEAD"))
    private void polydium$flushBeforeCommand(CallbackInfoReturnable<VkCommandBuffer> cir) {
        this.polydium$endSuspendedPass();

        if (this.polydium$deferDepth == 0) {
            this.polydium$flushClears();
            this.polydium$flushBarrier();
        }
    }

    @Inject(method = "endCommandBuffer", at = @At("HEAD"))
    private void polydium$flushBeforeEnd(CallbackInfo ci) {
        this.polydium$endSuspendedPass();
        this.polydium$flushClears();
        this.polydium$flushBarrier();
    }

    @Inject(method = "clearDepthTexture", at = @At("HEAD"), cancellable = true)
    private void polydium$deferDepthClear(GpuTexture depthTexture, double clearDepth, CallbackInfo ci) {
        if (this.polydium$clearColorTexture != null || this.polydium$clearDepthTexture != null) {
            this.polydium$flushClears();
        }

        this.polydium$clearDepthTexture = depthTexture;
        this.polydium$clearDepth = clearDepth;
        ci.cancel();
    }

    @Inject(method = "clearColorAndDepthTextures(Lcom/mojang/renderpearl/api/textures/GpuTexture;Lorg/joml/Vector4fc;Lcom/mojang/renderpearl/api/textures/GpuTexture;D)V", at = @At("HEAD"), cancellable = true)
    private void polydium$deferColorAndDepthClear(GpuTexture colorTexture, Vector4fc clearColor, GpuTexture depthTexture, double clearDepth, CallbackInfo ci) {
        this.polydium$flushClears();
        this.polydium$clearColorTexture = colorTexture;
        this.polydium$clearColor = clearColor;
        this.polydium$clearDepthTexture = depthTexture;
        this.polydium$clearDepth = clearDepth;
        ci.cancel();
    }

    @ModifyVariable(method = "createRenderPass", at = @At("HEAD"), argsOnly = true)
    private RenderPassDescriptor polydium$clearOnLoad(RenderPassDescriptor descriptor) {
        if (this.polydium$clearColorTexture == null && this.polydium$clearDepthTexture == null) {
            return descriptor;
        }

        if (this.polydium$canContinue(descriptor)) {
            return descriptor;
        }

        RenderPass.RenderArea area = polydium$area(descriptor);
        List<RenderPassDescriptor.Attachment<Optional<Vector4fc>>> colors = new ArrayList<>(descriptor.colorAttachments());
        RenderPassDescriptor.Attachment<OptionalDouble> depth = descriptor.depthAttachment();

        for (int index = 0; index < colors.size(); index++) {
            RenderPassDescriptor.Attachment<Optional<Vector4fc>> color = colors.get(index);

            if (color == null || this.polydium$clearColorTexture == null || !polydium$coversTexture(color.textureView(), this.polydium$clearColorTexture, area)) {
                continue;
            }

            if (color.clearValue().isEmpty()) {
                colors.set(index, new RenderPassDescriptor.Attachment<>(color.textureView(), Optional.of(this.polydium$clearColor)));
            }

            this.polydium$clearColorTexture = null;
        }

        if (depth != null && this.polydium$clearDepthTexture != null && polydium$coversTexture(depth.textureView(), this.polydium$clearDepthTexture, area)) {
            if (depth.clearValue().isEmpty()) {
                depth = new RenderPassDescriptor.Attachment<>(depth.textureView(), OptionalDouble.of(this.polydium$clearDepth));
            }

            this.polydium$clearDepthTexture = null;
        }

        this.polydium$flushClears();

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

    @Inject(method = "createRenderPass", at = @At("HEAD"), cancellable = true)
    private void polydium$continueSuspendedPass(RenderPassDescriptor descriptor, CallbackInfoReturnable<RenderPassBackend> cir) {
        if (!this.polydium$canContinue(descriptor)) {
            if (RenderPassStats.ENABLED && this.polydium$suspendedPass != null) {
                RenderPassStats.refused(this.polydium$suspendedPass.getLabel(), descriptor.label(), this.polydium$barrierPending ? "upload barrier pending" : "attachments, area or clear differ");
            }

            return;
        }

        if (RenderPassStats.ENABLED) {
            RenderPassStats.merged();
        }

        OptionalDouble depthClear = descriptor.depthAttachment() == null ? OptionalDouble.empty() : descriptor.depthAttachment().clearValue();

        if (this.polydium$clearDepthTexture != null) {
            if (depthClear.isEmpty()) {
                depthClear = OptionalDouble.of(this.polydium$clearDepth);
            }

            this.polydium$clearDepthTexture = null;
        }

        VkCommandBuffer commandBuffer = this.currentCommandBuffer;
        Supplier<String> previousLabel = this.polydium$suspendedPass.getLabel();
        VulkanDebug debug = this.device.instance().debug();

        debug.endDebugGroup(commandBuffer);
        this.checkpointStorage.recordCheckpoint(commandBuffer, CheckpointExtension.CheckpointType.END_RENDER_PASS, previousLabel);
        debug.beginDebugGroup(commandBuffer, descriptor.label());
        this.checkpointStorage.recordCheckpoint(commandBuffer, CheckpointExtension.CheckpointType.BEGIN_RENDER_PASS, descriptor.label());

        this.polydium$suspendedPass = null;
        this.polydium$suspendedDescriptor = null;
        this.polydium$openDescriptor = descriptor;
        this.currentRenderPass = new VulkanRenderPass(this.device, (VulkanCommandEncoder) (Object) this, commandBuffer, this.checkpointStorage, polydium$area(descriptor), polydium$outputWidth(descriptor), polydium$outputHeight(descriptor), descriptor.depthAttachment() != null, descriptor.label());

        if (depthClear.isPresent()) {
            polydium$clearDepthInPass(commandBuffer, polydium$area(descriptor), (float) depthClear.getAsDouble());
        }

        cir.setReturnValue(this.currentRenderPass);
    }

    @Inject(method = "createRenderPass", at = @At("RETURN"))
    private void polydium$rememberDescriptor(RenderPassDescriptor descriptor, CallbackInfoReturnable<RenderPassBackend> cir) {
        this.polydium$openDescriptor = descriptor;

        if (RenderPassStats.ENABLED) {
            RenderPassStats.begun();
        }
    }

    @Inject(method = "submit", at = @At("RETURN"))
    private void polydium$reportPassStats(CallbackInfo ci) {
        if (RenderPassStats.ENABLED) {
            RenderPassStats.submit();
        }
    }

    @Inject(method = "submitRenderPass", at = @At("HEAD"), cancellable = true)
    private void polydium$suspendPass(CallbackInfo ci) {
        if (this.currentRenderPass == null || this.polydium$openDescriptor == null) {
            return;
        }

        if (this.polydium$suspendedPass != null) {
            throw new IllegalStateException("A render pass is already suspended");
        }

        this.polydium$suspendedPass = this.currentRenderPass;
        this.polydium$suspendedDescriptor = this.polydium$openDescriptor;
        this.polydium$openDescriptor = null;
        this.currentRenderPass = null;
        ci.cancel();
    }

    @Inject(method = "writeToBuffer", at = @At("HEAD"))
    private void polydium$beginUpload(GpuBufferSlice destination, ByteBuffer data, CallbackInfo ci) {
        this.polydium$deferDepth++;
        long buffer = ((VulkanGpuBuffer) destination.buffer()).vkBuffer();
        long start = destination.offset();
        long end = start + data.remaining();

        if (this.polydium$overlapsPending(buffer, start, end)) {
            this.polydium$flushBarrier();
        }

        this.polydium$uploadBuffer = buffer;
        this.polydium$uploadStart = start;
        this.polydium$uploadEnd = end;
        this.polydium$readBuffer = 0L;
    }

    @Inject(method = "copyToBuffer", at = @At("HEAD"))
    private void polydium$beginCopy(GpuBufferSlice source, GpuBufferSlice target, CallbackInfo ci) {
        this.polydium$deferDepth++;
        long sourceBuffer = ((VulkanGpuBuffer) source.buffer()).vkBuffer();
        long targetBuffer = ((VulkanGpuBuffer) target.buffer()).vkBuffer();
        long sourceStart = source.offset();
        long sourceEnd = sourceStart + source.length();
        long targetStart = target.offset();
        long targetEnd = targetStart + source.length();

        if (this.polydium$overlapsPending(sourceBuffer, sourceStart, sourceEnd) || this.polydium$overlapsPending(targetBuffer, targetStart, targetEnd)) {
            this.polydium$flushBarrier();
        }

        this.polydium$uploadBuffer = targetBuffer;
        this.polydium$uploadStart = targetStart;
        this.polydium$uploadEnd = targetEnd;
        this.polydium$readBuffer = sourceBuffer;
        this.polydium$readStart = sourceStart;
        this.polydium$readEnd = sourceEnd;
    }

    @Inject(method = "writeTimestamp", at = @At("HEAD"))
    private void polydium$beginTimestamp(CallbackInfo ci) {
        this.polydium$deferDepth++;
    }

    @Inject(method = {"writeTimestamp", "writeToBuffer", "copyToBuffer"}, at = @At("RETURN"))
    private void polydium$endDeferred(CallbackInfo ci) {
        this.polydium$deferDepth--;
    }

    @WrapOperation(method = {"writeToBuffer", "copyToBuffer"}, at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanCommandEncoder;memoryBarrier(Lorg/lwjgl/system/MemoryStack;)V"))
    private void polydium$deferUploadBarrier(VulkanCommandEncoder encoder, MemoryStack stack, Operation<Void> original) {
        this.polydium$pendingRanges.add(this.polydium$uploadBuffer);
        this.polydium$pendingRanges.add(this.polydium$uploadStart);
        this.polydium$pendingRanges.add(this.polydium$uploadEnd);

        if (this.polydium$readBuffer != 0L) {
            this.polydium$pendingRanges.add(this.polydium$readBuffer);
            this.polydium$pendingRanges.add(this.polydium$readStart);
            this.polydium$pendingRanges.add(this.polydium$readEnd);
        }

        this.polydium$barrierPending = true;
    }

    @Unique
    private boolean polydium$canContinue(RenderPassDescriptor descriptor) {
        RenderPassDescriptor previous = this.polydium$suspendedDescriptor;

        if (this.polydium$suspendedPass == null || this.polydium$barrierPending) {
            return false;
        }

        if (this.polydium$clearColorTexture != null) {
            return false;
        }

        if (!polydium$area(descriptor).equals(polydium$area(previous)) || descriptor.colorAttachments().size() != previous.colorAttachments().size()) {
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
            return depth == previousDepth && this.polydium$clearDepthTexture == null;
        }

        if (depth.textureView() != previousDepth.textureView()) {
            return false;
        }

        return this.polydium$clearDepthTexture == null || polydium$coversTexture(depth.textureView(), this.polydium$clearDepthTexture, polydium$area(descriptor));
    }

    @Unique
    private static void polydium$clearDepthInPass(VkCommandBuffer commandBuffer, RenderPass.RenderArea area, float depth) {
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

    @Unique
    private void polydium$endSuspendedPass() {
        VulkanRenderPass pass = this.polydium$suspendedPass;

        if (pass == null) {
            return;
        }

        if (RenderPassStats.ENABLED) {
            RenderPassStats.split(pass.getLabel());
        }

        VkCommandBuffer commandBuffer = this.currentCommandBuffer;

        this.polydium$suspendedPass = null;
        this.polydium$suspendedDescriptor = null;

        KHRDynamicRendering.vkCmdEndRenderingKHR(commandBuffer);
        this.device.instance().debug().endDebugGroup(commandBuffer);
        this.checkpointStorage.recordCheckpoint(commandBuffer, CheckpointExtension.CheckpointType.END_RENDER_PASS, pass.getLabel());

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VulkanCommandEncoder.memoryBarrier(commandBuffer, stack);
        }
    }

    @Unique
    private void polydium$flushClears() {
        this.polydium$endSuspendedPass();

        GpuTexture color = this.polydium$clearColorTexture;
        GpuTexture depth = this.polydium$clearDepthTexture;

        if (color == null && depth == null) {
            return;
        }

        this.polydium$clearColorTexture = null;
        this.polydium$clearDepthTexture = null;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            if (color != null) {
                this.clearColorTextureUnsynced(stack, color, this.polydium$clearColor);
            }

            if (depth != null) {
                this.clearDepthTextureUnsynced(stack, depth, this.polydium$clearDepth);
            }

            this.memoryBarrier(stack);
        }
    }

    @Unique
    private void polydium$flushBarrier() {
        this.polydium$endSuspendedPass();

        if (!this.polydium$barrierPending) {
            return;
        }

        if (this.currentCommandBuffer == null) {
            throw new IllegalStateException("Upload barrier pending without a recording command buffer");
        }

        this.polydium$barrierPending = false;
        this.polydium$pendingRanges.clear();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VulkanCommandEncoder.memoryBarrier(this.currentCommandBuffer, stack);
        }
    }

    @Unique
    private boolean polydium$overlapsPending(long buffer, long start, long end) {
        LongArrayList ranges = this.polydium$pendingRanges;

        for (int index = 0; index < ranges.size(); index += 3) {
            if (ranges.getLong(index) == buffer && ranges.getLong(index + 1) < end && start < ranges.getLong(index + 2)) {
                return true;
            }
        }

        return false;
    }

    @Unique
    private static RenderPass.RenderArea polydium$area(RenderPassDescriptor descriptor) {
        //? if >= 26.3 {
        return descriptor.renderArea();
        //?} else {
        /*return descriptor.renderArea;
        *///?}
    }

    @Unique
    private static int polydium$outputWidth(RenderPassDescriptor descriptor) {
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

    @Unique
    private static int polydium$outputHeight(RenderPassDescriptor descriptor) {
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

    @Unique
    private static boolean polydium$coversTexture(GpuTextureView view, GpuTexture texture, RenderPass.RenderArea area) {
        return view.texture() == texture && texture.getMipLevels() == 1 && view.baseMipLevel() == 0 && view.mipLevels() == 1 && area.fillsTexture(view);
    }
}
//?}
