package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import java.nio.ByteBuffer;
import java.util.List;
import org.lwjgl.opengl.GL11;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.polyfrost.polydium.vulkan.XaeroVulkan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = {
    "xaero.map.graphics.OpenGlHelper",
    "xaero.map.graphics.TextureUploader",
    "xaero.map.graphics.TextureUploadBenchmark",
    "xaero.map.graphics.GpuObjectDeleter",
    "xaero.map.MapLimiter",
    "xaero.map.file.export.PNGExporter",
    "xaero.map.region.MapTileChunk",
    "xaero.map.highlight.DimensionHighlighterHandler",
    "xaero.map.region.LeveledRegion",
    "xaero.map.region.texture.RegionTexture"
}, remap = false)
public class MixinVulkanXaeroWorldMap {

    @Inject(method = "copyTextureToBGRAPackBuffer", at = @At("HEAD"), cancellable = true, require = 0)
    private static void polydium$download(GpuTexture texture, GpuBuffer packBuffer, long packBufferOffset, CallbackInfo ci) {
        if (!VulkanActive.active) return;
        RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(texture, packBuffer, packBufferOffset, () -> {}, 0);
        ci.cancel();
    }

    @Inject(method = "downloadMapTextureToBGRABuffer", at = @At("HEAD"), cancellable = true, require = 0)
    private static void polydium$downloadSync(GpuTexture texture, ByteBuffer out, CallbackInfo ci) {
        if (!VulkanActive.active) return;
        XaeroVulkan.downloadToBgra(texture, out);
        ci.cancel();
    }

    @Inject(method = "copyBGRAUnpackBufferToMapTexture", at = @At("HEAD"), cancellable = true, require = 0)
    private static void polydium$upload(GpuBuffer unpackBuffer, GpuTexture texture, int level, TextureFormat internalFormat, int width, int height, int border, long offset, CallbackInfo ci) {
        if (!VulkanActive.active) return;
        XaeroVulkan.copyBufferToTexture(unpackBuffer, texture, 0, 0, width, height);
        ci.cancel();
    }

    @Inject(method = "copyBGRAUnpackBufferToSubMapTexture", at = @At("HEAD"), cancellable = true, require = 0)
    private static void polydium$uploadSub(GpuBuffer unpackBuffer, GpuTexture texture, int level, int xOffset, int yOffset, int width, int height, long offset, CallbackInfo ci) {
        if (!VulkanActive.active) return;
        XaeroVulkan.copyBufferToTexture(unpackBuffer, texture, xOffset, yOffset, width, height);
        ci.cancel();
    }

    @Inject(method = "uploadBGRABufferToMapTexture", at = @At("HEAD"), cancellable = true, require = 0)
    private static void polydium$write(ByteBuffer colorBuffer, GpuTexture texture, TextureFormat internalFormat, int width, int height, CallbackInfo ci) {
        if (!VulkanActive.active) return;
        XaeroVulkan.writeToTexture(texture, colorBuffer, width, height);
        ci.cancel();
    }

    @Redirect(method = {"uploadTextures", "post"}, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glFinish()V"), require = 0)
    private void polydium$finish() {
        if (VulkanActive.active) XaeroVulkan.finish();
        else GL11.glFinish();
    }

    @Inject(method = "deleteBuffers(Ljava/util/List;I)V", at = @At("HEAD"), cancellable = true, require = 0)
    private static void polydium$deleteBuffers(List<GpuBuffer> buffers, int count, CallbackInfo ci) {
        if (!VulkanActive.active || buffers == null) return;
        for (int i = 0; i < count && !buffers.isEmpty(); i++) buffers.remove(buffers.size() - 1).close();
        ci.cancel();
    }

    @Inject(method = {"unbindPackBuffer", "unbindUnpackBuffer"}, at = @At("HEAD"), cancellable = true, require = 0)
    private static void polydium$skipUnbind(CallbackInfo ci) {
        if (VulkanActive.active) ci.cancel();
    }

    @Redirect(method = "deleteTextures", at = @At(value = "INVOKE", target = "Lxaero/map/graphics/OpenGlHelper;isUsingOpenGL()Z"), require = 0)
    private static boolean polydium$deleteTexturesAnyBackend() {
        return true;
    }

    @Inject(method = "updateAvailableVRAM", at = @At("HEAD"), cancellable = true, require = 0)
    private void polydium$skipVram(CallbackInfo ci) {
        if (VulkanActive.active) ci.cancel();
    }

    @Redirect(method = "export", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glGetInteger(I)I"), require = 0)
    private int polydium$maxTextureSize(int pname) {
        return VulkanActive.active ? RenderSystem.getDevice().getMaxTextureSize() : GL11.glGetInteger(pname);
    }

    @Redirect(method = "export", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/buffers/GpuFence;awaitCompletion(J)Z"), require = 0)
    private boolean polydium$submitThenAwait(GpuFence fence, long timeout) {
        if (VulkanActive.active) XaeroVulkan.submit();
        return fence.awaitCompletion(timeout);
    }

    @Redirect(method = {"putColour", "applyChunkHighlightColors"}, at = @At(value = "INVOKE", target = "Ljava/nio/ByteBuffer;putInt(II)Ljava/nio/ByteBuffer;"), require = 0)
    private ByteBuffer polydium$putColour(ByteBuffer buffer, int pos, int colour) {
        return VulkanActive.active ? XaeroVulkan.putColour(buffer, pos, colour) : buffer.putInt(pos, colour);
    }

    @Redirect(method = "applyChunkHighlightColors", at = @At(value = "INVOKE", target = "Ljava/nio/ByteBuffer;getInt(I)I"), require = 0)
    private int polydium$getColour(ByteBuffer buffer, int pos) {
        return VulkanActive.active ? XaeroVulkan.getColour(buffer, pos) : buffer.getInt(pos);
    }
}
