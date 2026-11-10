package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.8 {
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.BakedChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import org.polyfrost.polydium.client.perf.SectionBounds;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers", remap = false)
public abstract class MixinOcclusionBoundsBuffers {
    @Shadow @Final private Reference2ReferenceOpenHashMap<TerrainRenderPass, BakedChunkModelBuilder> builders;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void polydium$tagPasses(CallbackInfo ci) {
        for (Reference2ReferenceMap.Entry<TerrainRenderPass, BakedChunkModelBuilder> entry : this.builders.reference2ReferenceEntrySet()) {
            int     pass        = System.identityHashCode(entry.getKey());
            boolean translucent = entry.getKey().isTranslucent();

            for (ModelQuadFacing facing : ModelQuadFacing.VALUES)
                ((SectionBounds.PassHolder) entry.getValue().getVertexBuffer(facing)).polydium$setPass(pass, translucent);
        }
    }
}
//?}
