package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPassBackend;
import com.mojang.blaze3d.textures.GpuTexture;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.polyfrost.polydium.client.perf.VulkanDeferral;
import org.polyfrost.polydium.vulkan.VulkanCommandEncoder;
import org.polyfrost.polydium.vulkan.VulkanDevice;
import org.polyfrost.polydium.vulkan.VulkanRenderPass;
import org.polyfrost.polydium.vulkan.checkpoints.CheckpointExtension;
import org.polyfrost.polydium.vulkan.compat.RenderPassDescriptor;
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
public abstract class MixinVulkanDeferralBackport implements VulkanDeferral.Host {
    //? if >= 26.1 {
    @Unique
    private static final String CREATE_RENDER_PASS = "createRenderPass(Lorg/polyfrost/polydium/vulkan/compat/RenderPassDescriptor;)Lcom/mojang/blaze3d/systems/RenderPassBackend;";
    //?} else {
    /*@Unique
    private static final String CREATE_RENDER_PASS = "createRenderPass(Lorg/polyfrost/polydium/vulkan/compat/RenderPassDescriptor;)Lorg/polyfrost/polydium/vulkan/compat/RenderPassBackend;";
    *///?}

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

    @WrapOperation(method = "clearDepthTexture", at = @At(value = "INVOKE", target = "Lorg/polyfrost/polydium/vulkan/VulkanCommandEncoder;clearDepthTextureUnsynced(Lorg/lwjgl/system/MemoryStack;Lcom/mojang/blaze3d/textures/GpuTexture;D)V"))
    private void polydium$deferDepthClear(VulkanCommandEncoder encoder, MemoryStack stack, GpuTexture depthTexture, double clearDepth, Operation<Void> original) {
        this.polydium$deferral.deferDepthClear(depthTexture, clearDepth);
    }

    @WrapOperation(method = "clearColorAndDepthTextures(Lcom/mojang/blaze3d/textures/GpuTexture;Lorg/joml/Vector4fc;Lcom/mojang/blaze3d/textures/GpuTexture;D)V", at = @At(value = "INVOKE", target = "Lorg/polyfrost/polydium/vulkan/VulkanCommandEncoder;clearColorTextureUnsynced(Lorg/lwjgl/system/MemoryStack;Lcom/mojang/blaze3d/textures/GpuTexture;Lorg/joml/Vector4fc;)V"))
    private void polydium$deferColorClear(VulkanCommandEncoder encoder, MemoryStack stack, GpuTexture colorTexture, Vector4fc clearColor, Operation<Void> original) {
        this.polydium$deferral.deferColorClear(colorTexture, clearColor);
    }

    @WrapOperation(method = "clearColorAndDepthTextures(Lcom/mojang/blaze3d/textures/GpuTexture;Lorg/joml/Vector4fc;Lcom/mojang/blaze3d/textures/GpuTexture;D)V", at = @At(value = "INVOKE", target = "Lorg/polyfrost/polydium/vulkan/VulkanCommandEncoder;clearDepthTextureUnsynced(Lorg/lwjgl/system/MemoryStack;Lcom/mojang/blaze3d/textures/GpuTexture;D)V"))
    private void polydium$deferPairedDepthClear(VulkanCommandEncoder encoder, MemoryStack stack, GpuTexture depthTexture, double clearDepth, Operation<Void> original) {
        this.polydium$deferral.deferPairedDepthClear(depthTexture, clearDepth);
    }

    @WrapWithCondition(method = {"clearDepthTexture", "clearColorAndDepthTextures(Lcom/mojang/blaze3d/textures/GpuTexture;Lorg/joml/Vector4fc;Lcom/mojang/blaze3d/textures/GpuTexture;D)V"}, at = @At(value = "INVOKE", target = "Lorg/polyfrost/polydium/vulkan/VulkanCommandEncoder;memoryBarrier(Lorg/lwjgl/system/MemoryStack;)V"))
    private boolean polydium$deferClearBarrier(VulkanCommandEncoder encoder, MemoryStack stack) {
        return false;
    }

    @ModifyVariable(method = CREATE_RENDER_PASS, at = @At("HEAD"), argsOnly = true)
    private RenderPassDescriptor polydium$clearOnLoad(RenderPassDescriptor descriptor) {
        return this.polydium$deferral.clearOnLoad(descriptor);
    }

    @WrapMethod(method = CREATE_RENDER_PASS)
    private RenderPassBackend polydium$continueSuspendedPass(RenderPassDescriptor descriptor, Operation<RenderPassBackend> original) {
        VulkanRenderPass pass = this.polydium$deferral.continueSuspendedPass(descriptor);
        return pass != null ? pass : original.call(descriptor);
    }

    @Inject(method = CREATE_RENDER_PASS, at = @At("RETURN"))
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

    @WrapOperation(method = {"writeToBuffer", "copyToBuffer"}, at = @At(value = "INVOKE", target = "Lorg/polyfrost/polydium/vulkan/VulkanCommandEncoder;memoryBarrier(Lorg/lwjgl/system/MemoryStack;)V"))
    private void polydium$deferUploadBarrier(VulkanCommandEncoder encoder, MemoryStack stack, Operation<Void> original) {
        this.polydium$deferral.deferUploadBarrier();
    }
}
