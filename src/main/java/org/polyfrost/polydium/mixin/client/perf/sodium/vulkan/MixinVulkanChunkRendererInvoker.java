package org.polyfrost.polydium.mixin.client.perf.sodium.vulkan;

import net.caffeinemc.mods.sodium.client.gl.device.MultiDrawBatch;
import net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.data.SectionRenderDataStorage;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.viewport.CameraTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = DefaultChunkRenderer.class, remap = false)
public interface MixinVulkanChunkRendererInvoker {
    @Invoker("fillCommandBuffer")
    static void polydium$fillCommandBuffer(MultiDrawBatch batch, RenderRegion renderRegion, SectionRenderDataStorage renderDataStorage,
                                           ChunkRenderList renderList, CameraTransform camera, TerrainRenderPass pass,
                                           boolean useBlockFaceCulling, boolean useIndexedTessellation) {
        throw new AssertionError();
    }
}
