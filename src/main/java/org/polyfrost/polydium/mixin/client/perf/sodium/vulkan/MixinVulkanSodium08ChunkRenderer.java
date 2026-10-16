package org.polyfrost.polydium.mixin.client.perf.sodium.vulkan;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.textures.GpuSampler;
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

@Mixin(value = DefaultChunkRenderer.class, remap = false)
public abstract class MixinVulkanSodium08ChunkRenderer {
    @Shadow
    @Final
    private SharedQuadIndexBuffer sharedIndexBuffer;

    @WrapMethod(method = "render")
    private void polydium$renderVulkan(ChunkRenderMatrices matrices, CommandList commandList, ChunkRenderListIterable renderLists,
                                       TerrainRenderPass renderPass, CameraTransform camera, FogParameters parameters,
                                       boolean indexedRenderingEnabled, GpuSampler terrainSampler, Operation<Void> original) {
        if (!VulkanActive.active) {
            original.call(matrices, commandList, renderLists, renderPass, camera, parameters, indexedRenderingEnabled, terrainSampler);
            return;
        }

        SodiumVulkan.render(commandList, renderLists, renderPass, camera, indexedRenderingEnabled, this.sharedIndexBuffer,
                terrainSampler, SodiumVulkan.globals08(matrices, parameters));
    }
}
