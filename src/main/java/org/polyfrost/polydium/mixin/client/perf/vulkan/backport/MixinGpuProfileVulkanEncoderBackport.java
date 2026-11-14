package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import org.polyfrost.polydium.client.perf.gpuprofile.GpuProfiler;
import org.polyfrost.polydium.vulkan.VulkanCommandEncoder;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VulkanCommandEncoder.class, remap = false)
public abstract class MixinGpuProfileVulkanEncoderBackport {
    @Shadow
    private VkCommandBuffer currentCommandBuffer;

    @Unique
    private VkCommandBuffer polydium$profiledBuffer;

    @Inject(method = {"clearColorTexture", "clearColorAndDepthTextures", "clearDepthTexture"}, at = @At("HEAD"), require = 0)
    private void polydium$profileClear(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.beginOp(polydium$profiledBuffer = currentCommandBuffer != null ? currentCommandBuffer : ((VulkanCommandEncoder) (Object) this).commandBuffer(), GpuProfiler.OP_CLEAR);
    }

    @Inject(method = {"writeToBuffer", "copyToBuffer", "writeToTexture", "copyBufferToTexture"}, at = @At("HEAD"), require = 0, order = 1100)
    private void polydium$profileUpload(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.beginOp(polydium$profiledBuffer = ((VulkanCommandEncoder) (Object) this).commandBuffer(), GpuProfiler.OP_UPLOAD);
    }

    @Inject(method = {"copyTextureToBuffer", "copyTextureToTexture"}, at = @At("HEAD"), require = 0)
    private void polydium$profileCopy(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.beginOp(polydium$profiledBuffer = ((VulkanCommandEncoder) (Object) this).commandBuffer(), GpuProfiler.OP_COPY);
    }

    @Inject(method = {"clearColorTexture", "clearColorAndDepthTextures", "clearDepthTexture", "writeToBuffer", "copyToBuffer", "writeToTexture",
        "copyBufferToTexture", "copyTextureToBuffer", "copyTextureToTexture"}, at = @At("RETURN"), require = 0)
    private void polydium$profileOpEnd(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.end(polydium$profiledBuffer);
    }
}
