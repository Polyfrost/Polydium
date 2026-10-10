package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.lists.VisibleChunkCollector", remap = false)
public abstract class MixinVisibleRegionLookup {
    @Unique
    private boolean polydium$hasRegion;
    @Unique
    private int polydium$regionX;
    @Unique
    private int polydium$regionY;
    @Unique
    private int polydium$regionZ;
    @Unique
    private RenderRegion polydium$region;

    @WrapOperation(method = "visit", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/region/RenderRegionManager;getForChunk(III)Lnet/caffeinemc/mods/sodium/client/render/chunk/region/RenderRegion;"))
    private RenderRegion polydium$lastRegion(RenderRegionManager regions, int x, int y, int z, Operation<RenderRegion> original) {
        int regionX = x >> RenderRegion.REGION_WIDTH_SH;
        int regionY = y >> RenderRegion.REGION_HEIGHT_SH;
        int regionZ = z >> RenderRegion.REGION_LENGTH_SH;
        if (this.polydium$hasRegion && regionX == this.polydium$regionX && regionY == this.polydium$regionY && regionZ == this.polydium$regionZ) {
            return this.polydium$region;
        }
        this.polydium$region = original.call(regions, x, y, z);
        this.polydium$regionX = regionX;
        this.polydium$regionY = regionY;
        this.polydium$regionZ = regionZ;
        this.polydium$hasRegion = true;
        return this.polydium$region;
    }
}
//?}
