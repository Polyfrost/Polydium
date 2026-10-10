package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import net.minecraft.client.renderer.WeatherEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;

//? if < 26.2 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.polyfrost.polydium.client.perf.PrecipitationCache;
import org.spongepowered.asm.mixin.injection.At;
*///?}

@Mixin(WeatherEffectRenderer.class)
public class MixinPrecipitationCacheWeather {
    //? if < 26.2 {
    /*@WrapOperation(method = "getPrecipitationAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;"))
    private Holder<Biome> polydium$cachedBiome(Level level, BlockPos pos, Operation<Holder<Biome>> original) {
        if (!RenderSystem.isOnRenderThread() || !(level instanceof ClientLevel clientLevel)) {
            return original.call(level, pos);
        }
        return ((PrecipitationCache.Access) clientLevel).polydium$precipitationCache().biome(clientLevel, pos, original);
    }

    @WrapOperation(method = "getPrecipitationAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;getPrecipitationAt(Lnet/minecraft/core/BlockPos;I)Lnet/minecraft/world/level/biome/Biome$Precipitation;"))
    private Biome.Precipitation polydium$cachedPrecipitation(Biome biome, BlockPos pos, int seaLevel, Operation<Biome.Precipitation> original, @Local(argsOnly = true) Level level) {
        if (!RenderSystem.isOnRenderThread() || !(level instanceof ClientLevel)) {
            return original.call(biome, pos, seaLevel);
        }
        return ((PrecipitationCache.Access) level).polydium$precipitationCache().precipitation(biome, pos, seaLevel, original);
    }
    *///?}
}
//?}
