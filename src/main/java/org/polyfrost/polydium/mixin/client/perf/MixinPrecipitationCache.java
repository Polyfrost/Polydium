package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.polyfrost.polydium.client.perf.PrecipitationCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientLevel.class)
public class MixinPrecipitationCache implements PrecipitationCache.Access {
    @Unique
    private final PrecipitationCache polydium$precipitationCache = new PrecipitationCache();

    @Override
    public PrecipitationCache polydium$precipitationCache() {
        return this.polydium$precipitationCache;
    }

    //? if >= 26.2 {
    @WrapOperation(method = "getPrecipitationAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;"))
    private Holder<Biome> polydium$cachedBiome(ClientLevel level, BlockPos pos, Operation<Holder<Biome>> original) {
        if (!RenderSystem.isOnRenderThread()) {
            return original.call(level, pos);
        }
        return this.polydium$precipitationCache.biome(level, pos, original);
    }

    @WrapOperation(method = "getPrecipitationAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;getPrecipitationAt(Lnet/minecraft/core/BlockPos;I)Lnet/minecraft/world/level/biome/Biome$Precipitation;"))
    private Biome.Precipitation polydium$cachedPrecipitation(Biome biome, BlockPos pos, int seaLevel, Operation<Biome.Precipitation> original) {
        if (!RenderSystem.isOnRenderThread()) {
            return original.call(biome, pos, seaLevel);
        }
        return this.polydium$precipitationCache.precipitation(biome, pos, seaLevel, original);
    }
    //?}
}
//?}
