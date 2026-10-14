package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPass;
import com.mojang.renderpearl.backend.vulkan.checkpoints.CheckpointExtension;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.polyfrost.polydium.client.perf.VulkanDeferral;
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

@Mixin(VulkanCommandEncoder.class)
public abstract class MixinVulkanDeferral implements VulkanDeferral.Host {
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
    private final VulkanDeferral polydium$deferral = new VulkanDeferral(this);

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

    @Override
    public VkCommandBuffer polydium$currentCommandBuffer() {
        return this.currentCommandBuffer;
    }

    @Override
    public VulkanRenderPass polydium$currentRenderPass() {
        return this.currentRenderPass;
    }

    @Override
    public void polydium$setCurrentRenderPass(VulkanRenderPass pass) {
        this.currentRenderPass = pass;
    }

    @Override
    public VulkanDevice polydium$device() {
        return this.device;
    }

    @Override
    public CheckpointExtension.CheckpointStorage polydium$checkpointStorage() {
        return this.checkpointStorage;
    }

    @Override
    public void polydium$clearColorUnsynced(MemoryStack stack, GpuTexture colorTexture, Vector4fc clearColor) {
        this.clearColorTextureUnsynced(stack, colorTexture, clearColor);
    }

    @Override
    public void polydium$clearDepthUnsynced(MemoryStack stack, GpuTexture depthTexture, double clearDepth) {
        this.clearDepthTextureUnsynced(stack, depthTexture, clearDepth);
    }

    @Override
    public void polydium$memoryBarrier(MemoryStack stack) {
        this.memoryBarrier(stack);
    }

    @Inject(method = "commandBuffer", at = @At("HEAD"))
    private void polydium$flushBeforeCommand(CallbackInfoReturnable<VkCommandBuffer> cir) {
        this.polydium$deferral.flushBeforeCommand();
    }

    @Inject(method = "endCommandBuffer", at = @At("HEAD"))
    private void polydium$flushBeforeEnd(CallbackInfo ci) {
        this.polydium$deferral.flushBeforeEnd();
    }

    @WrapOperation(method = "clearDepthTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanCommandEncoder;clearDepthTextureUnsynced(Lorg/lwjgl/system/MemoryStack;Lcom/mojang/renderpearl/api/textures/GpuTexture;D)V"))
    private void polydium$deferDepthClear(VulkanCommandEncoder encoder, MemoryStack stack, GpuTexture depthTexture, double clearDepth, Operation<Void> original) {
        this.polydium$deferral.deferDepthClear(depthTexture, clearDepth);
    }

    @WrapOperation(method = "clearColorAndDepthTextures(Lcom/mojang/renderpearl/api/textures/GpuTexture;Lorg/joml/Vector4fc;Lcom/mojang/renderpearl/api/textures/GpuTexture;D)V", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanCommandEncoder;clearColorTextureUnsynced(Lorg/lwjgl/system/MemoryStack;Lcom/mojang/renderpearl/api/textures/GpuTexture;Lorg/joml/Vector4fc;)V"))
    private void polydium$deferColorClear(VulkanCommandEncoder encoder, MemoryStack stack, GpuTexture colorTexture, Vector4fc clearColor, Operation<Void> original) {
        this.polydium$deferral.deferColorClear(colorTexture, clearColor);
    }

    @WrapOperation(method = "clearColorAndDepthTextures(Lcom/mojang/renderpearl/api/textures/GpuTexture;Lorg/joml/Vector4fc;Lcom/mojang/renderpearl/api/textures/GpuTexture;D)V", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanCommandEncoder;clearDepthTextureUnsynced(Lorg/lwjgl/system/MemoryStack;Lcom/mojang/renderpearl/api/textures/GpuTexture;D)V"))
    private void polydium$deferPairedDepthClear(VulkanCommandEncoder encoder, MemoryStack stack, GpuTexture depthTexture, double clearDepth, Operation<Void> original) {
        this.polydium$deferral.deferPairedDepthClear(depthTexture, clearDepth);
    }

    @WrapWithCondition(method = {"clearDepthTexture", "clearColorAndDepthTextures(Lcom/mojang/renderpearl/api/textures/GpuTexture;Lorg/joml/Vector4fc;Lcom/mojang/renderpearl/api/textures/GpuTexture;D)V"}, at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanCommandEncoder;memoryBarrier(Lorg/lwjgl/system/MemoryStack;)V"))
    private boolean polydium$deferClearBarrier(VulkanCommandEncoder encoder, MemoryStack stack) {
        return false;
    }

    @ModifyVariable(method = "createRenderPass", at = @At("HEAD"), argsOnly = true)
    private RenderPassDescriptor polydium$clearOnLoad(RenderPassDescriptor descriptor) {
        return this.polydium$deferral.clearOnLoad(descriptor);
    }

    @WrapMethod(method = "createRenderPass")
    private RenderPassBackend polydium$continueSuspendedPass(RenderPassDescriptor descriptor, Operation<RenderPassBackend> original) {
        VulkanRenderPass pass = this.polydium$deferral.continueSuspendedPass(descriptor);
        return pass != null ? pass : original.call(descriptor);
    }

    @Inject(method = "createRenderPass", at = @At("RETURN"))
    private void polydium$rememberDescriptor(RenderPassDescriptor descriptor, CallbackInfoReturnable<RenderPassBackend> cir) {
        this.polydium$deferral.rememberDescriptor(descriptor);
    }

    @Inject(method = "submit", at = @At("RETURN"))
    private void polydium$reportPassStats(CallbackInfo ci) {
        this.polydium$deferral.submitted();
    }

    @WrapMethod(method = "submitRenderPass")
    private void polydium$suspendPass(Operation<Void> original) {
        if (!this.polydium$deferral.suspendPass()) {
            original.call();
        }
    }

    @Inject(method = "writeToBuffer", at = @At("HEAD"))
    private void polydium$beginUpload(GpuBufferSlice destination, ByteBuffer data, CallbackInfo ci) {
        this.polydium$deferral.beginUpload(destination, data);
    }

    @Inject(method = "copyToBuffer", at = @At("HEAD"))
    private void polydium$beginCopy(GpuBufferSlice source, GpuBufferSlice target, CallbackInfo ci) {
        this.polydium$deferral.beginCopy(source, target);
    }

    @Inject(method = "writeTimestamp", at = @At("HEAD"))
    private void polydium$beginTimestamp(CallbackInfo ci) {
        this.polydium$deferral.beginTimestamp();
    }

    @Inject(method = {"writeTimestamp", "writeToBuffer", "copyToBuffer"}, at = @At("RETURN"))
    private void polydium$endDeferred(CallbackInfo ci) {
        this.polydium$deferral.endDeferred();
    }

    @WrapOperation(method = {"writeToBuffer", "copyToBuffer"}, at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanCommandEncoder;memoryBarrier(Lorg/lwjgl/system/MemoryStack;)V"))
    private void polydium$deferUploadBarrier(VulkanCommandEncoder encoder, MemoryStack stack, Operation<Void> original) {
        this.polydium$deferral.deferUploadBarrier();
    }
}
//?}
