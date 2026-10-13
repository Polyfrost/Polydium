package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import org.polyfrost.polydium.client.perf.PerfClock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Particle.class)
public class MixinParticleLightCache {
    @Shadow
    protected double x;
    @Shadow
    protected double y;
    @Shadow
    protected double z;

    @Unique
    private long polydium$lightTick = -1L;
    @Unique
    private double polydium$lightX;
    @Unique
    private double polydium$lightY;
    @Unique
    private double polydium$lightZ;
    @Unique
    private int polydium$light;

    @WrapOperation(method = /*? if >= 26.1 {*/"getLightCoords(F)I"/*?} else {*//*"getLightColor(F)I"*//*?}*/, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;hasChunkAt(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean polydium$skipCachedLookup(ClientLevel level, BlockPos pos, Operation<Boolean> original) {
        return !this.polydium$lightCached() && original.call(level, pos);
    }

    @ModifyReturnValue(method = /*? if >= 26.1 {*/"getLightCoords(F)I"/*?} else {*//*"getLightColor(F)I"*//*?}*/, at = @At("RETURN"))
    private int polydium$cacheLight(int light) {
        if (this.polydium$lightCached()) {
            return this.polydium$light;
        }
        this.polydium$lightTick = PerfClock.tick;
        this.polydium$lightX = this.x;
        this.polydium$lightY = this.y;
        this.polydium$lightZ = this.z;
        this.polydium$light = light;
        return light;
    }

    @Unique
    private boolean polydium$lightCached() {
        return this.polydium$lightTick == PerfClock.tick && this.polydium$lightX == this.x && this.polydium$lightY == this.y && this.polydium$lightZ == this.z;
    }

    @Unique
    private static final BlockPos.MutableBlockPos polydium$LIGHT_POS = new BlockPos.MutableBlockPos();

    @WrapOperation(
            //? if >= 26.1 {
            method = "getLightCoords(F)I",
            //?} else {
            /*method = "getLightColor(F)I",
            *///?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;containing(DDD)Lnet/minecraft/core/BlockPos;")
    )
    private BlockPos polydium$lightPosition(double x, double y, double z, Operation<BlockPos> original) {
        if (!RenderSystem.isOnRenderThread()) {
            return original.call(x, y, z);
        }
        return polydium$LIGHT_POS.set(x, y, z);
    }
}
//?}
