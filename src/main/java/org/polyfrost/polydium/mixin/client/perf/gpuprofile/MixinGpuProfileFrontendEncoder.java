package org.polyfrost.polydium.mixin.client.perf.gpuprofile;

//? if >= 26.2 {
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
//? if >= 26.3 {
import com.mojang.renderpearl.backend.api.CommandEncoderBackend;
//?} else
/*import com.mojang.blaze3d.systems.CommandEncoderBackend;*/
import org.lwjgl.vulkan.VkCommandBuffer;
import org.polyfrost.polydium.client.perf.gpuprofile.GpuProfiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = /*? if >= 26.3 {*/"com.mojang.renderpearl.frontend.FrontendCommandEncoder"/*?} else {*//*"com.mojang.renderpearl.api.commands.CommandEncoder"*//*?}*/)
public abstract class MixinGpuProfileFrontendEncoder {
    @Shadow
    @Final
    private CommandEncoderBackend backend;

    @Unique
    private Object polydium$clearContext() {
        if (this.backend instanceof VulkanCommandEncoderInvoker vulkan) {
            VkCommandBuffer buffer = vulkan.polydium$currentCommandBuffer();

            if (buffer != null)
                return buffer;
        }

        return this;
    }

    @Inject(method = "createRenderPass(Lcom/mojang/renderpearl/api/commands/RenderPassDescriptor;)Lcom/mojang/renderpearl/api/commands/RenderPass;", at = @At("HEAD"))
    private void polydium$profilePass(RenderPassDescriptor descriptor, CallbackInfoReturnable<?> cir) {
        if (GpuProfiler.recording)
            GpuProfiler.pass(descriptor.label());
    }

    @Inject(method = {"clearColorTexture", "clearColorAndDepthTextures", "clearDepthTexture"}, at = @At("HEAD"), require = 0)
    private void polydium$profileClear(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.beginOp(this.polydium$clearContext(), GpuProfiler.OP_CLEAR);
    }

    @Inject(method = {"writeToBuffer", "copyToBuffer", "writeToTexture", "copyBufferToTexture"}, at = @At("HEAD"), require = 0)
    private void polydium$profileUpload(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.beginOp(this, GpuProfiler.OP_UPLOAD);
    }

    @Inject(method = {"copyTextureToBuffer", "copyTextureToTexture"}, at = @At("HEAD"), require = 0)
    private void polydium$profileCopy(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.beginOp(this, GpuProfiler.OP_COPY);
    }

    @Inject(method = {"clearColorTexture", "clearColorAndDepthTextures", "clearDepthTexture"}, at = @At("RETURN"), require = 0)
    private void polydium$profileClearEnd(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.end(this.polydium$clearContext());
    }

    @Inject(method = {"writeToBuffer", "copyToBuffer", "writeToTexture", "copyBufferToTexture", "copyTextureToBuffer", "copyTextureToTexture"},
        at = @At("RETURN"), require = 0)
    private void polydium$profileOpEnd(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.end(this);
    }
}
//?}
