package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.8 {
import java.util.Map;
//? if >= 26.2 {
import net.caffeinemc.mods.sodium.client.gpu.device.batch.MultiDrawBatch;
//?} else {
/*import net.caffeinemc.mods.sodium.client.gl.device.MultiDrawBatch;
*///?}
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import org.polyfrost.polydium.client.perf.RegionOcclusion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion", remap = false)
public abstract class MixinOcclusionRegion implements RegionOcclusion {
    @Shadow
    @Final
    private Map<TerrainRenderPass, MultiDrawBatch> cachedBatches;

    @Shadow
    public abstract void clearAllCachedBatches();

    @Unique
    private final long[] polydium$hiddenSections = new long[RenderRegion.REGION_SIZE / Long.SIZE];
    @Unique
    private final long[] polydium$testedSections = new long[RenderRegion.REGION_SIZE / Long.SIZE];
    @Unique
    private final long[] polydium$sectionBounds = new long[RenderRegion.REGION_SIZE];
    @Unique
    private final long[] polydium$changedAt = new long[RenderRegion.REGION_SIZE];
    @Unique
    private final int[] polydium$animatedBounds = new int[RenderRegion.REGION_SIZE];
    @Unique
    private int polydium$testedGeneration;
    @Unique
    private long polydium$hiddenAfter;
    @Unique
    private int polydium$culledPasses;

    @Override
    public long[] polydium$getHiddenSections() {
        return this.polydium$hiddenSections;
    }

    @Override
    public long[] polydium$getTestedSections() {
        return this.polydium$testedSections;
    }

    @Override
    public long[] polydium$getSectionBounds() {
        return this.polydium$sectionBounds;
    }

    @Override
    public long[] polydium$getChangedAt() {
        return this.polydium$changedAt;
    }

    @Override
    public int[] polydium$getAnimatedBounds() {
        return this.polydium$animatedBounds;
    }

    @Override
    public int polydium$getTestedGeneration() {
        return this.polydium$testedGeneration;
    }

    @Override
    public void polydium$setTestedGeneration(int generation) {
        this.polydium$testedGeneration = generation;
    }

    @Override
    public long polydium$getHiddenAfter() {
        return this.polydium$hiddenAfter;
    }

    @Override
    public void polydium$setHiddenAfter(long captures) {
        this.polydium$hiddenAfter = captures;
    }

    @Override
    public void polydium$clearBatches() {
        int culled = this.polydium$culledPasses;

        for (int index = 0; index < DefaultTerrainRenderPasses.ALL.length; index ++) {
            MultiDrawBatch batch = this.cachedBatches.get(DefaultTerrainRenderPasses.ALL[index]);

            if (batch != null && batch.isFilled)
                culled |= 1 << index;
        }

        this.clearAllCachedBatches();
        this.polydium$culledPasses = culled;
    }

    @Override
    public boolean polydium$takeRefill(TerrainRenderPass pass) {
        int bit = polydium$bit(pass);

        if ((this.polydium$culledPasses & bit) == 0)
            return false;

        this.polydium$culledPasses &= ~bit;

        return true;
    }

    @Inject(method = "clearAllCachedBatches", at = @At("HEAD"))
    private void polydium$sodiumClearedAll(CallbackInfo ci) {
        this.polydium$culledPasses = 0;
    }

    @Inject(method = "clearCachedBatchFor", at = @At("HEAD"))
    private void polydium$sodiumCleared(TerrainRenderPass pass, CallbackInfo ci) {
        this.polydium$culledPasses &= ~polydium$bit(pass);
    }

    @Unique
    private static int polydium$bit(TerrainRenderPass pass) {
        for (int index = 0; index < DefaultTerrainRenderPasses.ALL.length; index ++) {
            if (DefaultTerrainRenderPasses.ALL[index] == pass)
                return 1 << index;
        }

        return 0;
    }
}
//?}
