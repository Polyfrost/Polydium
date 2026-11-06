package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.8 {
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import org.polyfrost.polydium.client.perf.RegionOcclusion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion", remap = false)
public abstract class MixinOcclusionRegion implements RegionOcclusion {
    @Unique
    private final long[] polydium$hiddenSections = new long[RenderRegion.REGION_SIZE / Long.SIZE];
    @Unique
    private final long[] polydium$testedSections = new long[RenderRegion.REGION_SIZE / Long.SIZE];
    @Unique
    private final long[] polydium$sectionBounds = new long[RenderRegion.REGION_SIZE];
    @Unique
    private final int[] polydium$animatedBounds = new int[RenderRegion.REGION_SIZE];
    @Unique
    private int polydium$testedGeneration;
    @Unique
    private long polydium$hiddenAfter;

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
}
//?}
