package org.polyfrost.polydium.mixin.client.perf.sodium.vulkan;

//? if >= 26.1 {
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.textures.GpuSampler;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlTexelBuffer;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.SharedQuadIndexBuffer;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderListIterable;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.viewport.CameraTransform;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.polyfrost.polydium.vulkan.sodium.SodiumVulkan;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DefaultChunkRenderer.class, remap = false)
public abstract class MixinVulkanChunkRenderer {
    @Shadow
    @Final
    private SharedQuadIndexBuffer sharedIndexBuffer;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void polydium$renderVulkan(ChunkRenderMatrices matrices, CommandList commandList, ChunkRenderListIterable renderLists,
                                       TerrainRenderPass renderPass, CameraTransform camera, FogParameters parameters,
                                       boolean indexedRenderingEnabled, GpuSampler terrainSampler, GpuBufferSlice uniformBuffer,
                                       GlTexelBuffer sectionTimeInfoTexture, CallbackInfo ci) {
        if (!VulkanActive.active)
            return;

        ci.cancel();
        SodiumVulkan.render(commandList, renderLists, renderPass, camera, indexedRenderingEnabled, this.sharedIndexBuffer,
                terrainSampler, uniformBuffer);
    }
}
//?}
