package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.3 {
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import org.polyfrost.polydium.client.perf.OcclusionCuller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager", remap = false)
public abstract class MixinOcclusionCull {
    @Shadow
    public abstract SortedRenderLists getRenderLists();

    @Inject(method = "prepareChunkRendering", at = @At("HEAD"))
    private void polydium$cullHiddenSections(ChunkRenderMatrices matrices, double x, double y, double z, boolean indexedRenderingEnabled, CallbackInfo ci) {
        OcclusionCuller.cull(this.getRenderLists(), matrices, x, y, z);
    }
}
//?}
