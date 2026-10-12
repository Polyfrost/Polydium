package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
//? if >= 1.21.4 {
import net.minecraft.client.renderer.WeatherEffectRenderer;
//?} else {
/*import net.minecraft.client.renderer.LevelRenderer;
*///?}
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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import org.polyfrost.polydium.client.perf.PrecipitationCache;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
*///?}

@Mixin(/*? if >= 1.21.4 {*/WeatherEffectRenderer/*?} else {*//*LevelRenderer*//*?}*/.class)
public class MixinPrecipitationCacheWeather {
    //? if >= 1.21.4 && < 26.2 {
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
    *///?} else if < 1.21.4 {
    /*@Shadow
    private ClientLevel level;

    @WrapOperation(method = "renderSnowAndRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;"))
    private Holder<Biome> polydium$cachedBiome(Level level, BlockPos pos, Operation<Holder<Biome>> original) {
        if (!RenderSystem.isOnRenderThread() || !(level instanceof ClientLevel clientLevel)) {
            return original.call(level, pos);
        }
        return ((PrecipitationCache.Access) clientLevel).polydium$precipitationCache().biome(clientLevel, pos, original);
    }

    @WrapOperation(method = "tickRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;"))
    private Holder<Biome> polydium$cachedRainBiome(LevelReader level, BlockPos pos, Operation<Holder<Biome>> original) {
        if (!RenderSystem.isOnRenderThread() || !(level instanceof ClientLevel clientLevel)) {
            return original.call(level, pos);
        }
        return ((PrecipitationCache.Access) clientLevel).polydium$precipitationCache().biome(clientLevel, pos, original);
    }

    @WrapOperation(method = {"renderSnowAndRain", "tickRain"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;getPrecipitationAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;"))
    private Biome.Precipitation polydium$cachedPrecipitation(Biome biome, BlockPos pos, Operation<Biome.Precipitation> original) {
        if (!RenderSystem.isOnRenderThread() || this.level == null) {
            return original.call(biome, pos);
        }
        return ((PrecipitationCache.Access) this.level).polydium$precipitationCache().precipitation(biome, pos, 63, original);
    }
    *///?}
}
//?}
