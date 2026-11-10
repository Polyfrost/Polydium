package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.2 {
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.data.BuiltSectionMeshParts;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.impl.CompactChunkVertex;
import org.lwjgl.system.MemoryUtil;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.polyfrost.polydium.client.perf.NearestPlaneOrder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers", remap = false)
public abstract class MixinSodiumNearestPlaneOrder {
    @Shadow @Final private ChunkVertexType vertexType;

    @Inject(method = "createMesh", at = @At("HEAD"))
    private void polydium$nearestPlaneFirst(TerrainRenderPass pass, int visibleSlices, boolean forceUnassigned, boolean sliceReordering, CallbackInfoReturnable<BuiltSectionMeshParts> cir) {
        if (!PolydiumToggles.nearestPlaneQuadOrder || pass.isTranslucent() || this.vertexType.getClass() != CompactChunkVertex.class)
            return;

        ChunkModelBuilder builder = ((ChunkBuildBuffers) (Object) this).get(pass);

        for (int facing = 0; facing < ModelQuadFacing.DIRECTIONS; facing++) {
            ChunkMeshBufferBuilder buffer = builder.getVertexBuffer(ModelQuadFacing.VALUES[facing]);

            if (!buffer.isEmpty())
                NearestPlaneOrder.sort(MemoryUtil.memAddress(buffer.slice()), buffer.count() / 4, facing);
        }
    }
}
//?}
