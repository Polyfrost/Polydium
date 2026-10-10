package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.lang.ref.WeakReference;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import org.polyfrost.polydium.client.perf.PerfClock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AtmosphericFogEnvironment.class)
public class MixinRainFogTickCache {
    @Unique
    private long polydium$rainFogTick = -1L;
    @Unique
    private long polydium$rainFogPos;
    @Unique
    private WeakReference<ClientLevel> polydium$rainFogLevel = new WeakReference<>(null);
    @Unique
    private Holder<Biome> polydium$rainFogBiome;
    @Unique
    private int polydium$rainFogSkyLight = -1;

    @WrapOperation(method = "updateRainFogState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;"))
    private Holder<Biome> polydium$reuseTickBiome(ClientLevel level, BlockPos pos, Operation<Holder<Biome>> original) {
        long tick = PerfClock.tick;
        long key = pos.asLong();
        if (tick == this.polydium$rainFogTick && key == this.polydium$rainFogPos && this.polydium$rainFogLevel.refersTo(level)) {
            return this.polydium$rainFogBiome;
        }
        Holder<Biome> biome = original.call(level, pos);
        if (!this.polydium$rainFogLevel.refersTo(level)) {
            this.polydium$rainFogLevel = new WeakReference<>(level);
        }
        this.polydium$rainFogTick = tick;
        this.polydium$rainFogPos = key;
        this.polydium$rainFogBiome = biome;
        this.polydium$rainFogSkyLight = -1;
        return biome;
    }

    @WrapOperation(method = "updateRainFogState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/lighting/LayerLightEventListener;getLightValue(Lnet/minecraft/core/BlockPos;)I"))
    private int polydium$reuseTickSkyLight(LayerLightEventListener listener, BlockPos pos, Operation<Integer> original) {
        if (this.polydium$rainFogSkyLight < 0) {
            this.polydium$rainFogSkyLight = original.call(listener, pos);
        }
        return this.polydium$rainFogSkyLight;
    }
}
//?}
