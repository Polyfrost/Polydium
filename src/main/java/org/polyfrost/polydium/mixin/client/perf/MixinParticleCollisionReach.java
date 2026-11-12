package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import net.minecraft.world.level.chunk.PalettedContainer;
import org.polyfrost.polydium.client.perf.ParticleAirCollision;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.invoke.VarHandle;

@Mixin(PalettedContainer.class)
public abstract class MixinParticleCollisionReach implements ParticleAirCollision.Reach {
    @Unique
    private int polydium$paletteVersion;

    @Unique
    private int polydium$reachCode = -1;

    @Override
    public int polydium$paletteVersion() {
        return this.polydium$paletteVersion;
    }

    @Override
    public int polydium$reachCode() {
        return this.polydium$reachCode;
    }

    @Override
    public void polydium$setReachCode(int code) {
        this.polydium$reachCode = code;
    }

    @Override
    public void polydium$bumpPaletteVersion() {
        VarHandle.releaseFence();
        this.polydium$paletteVersion++;
    }

    @Override
    public void polydium$detachPaletteVersion() {
        VarHandle.releaseFence();
        this.polydium$paletteVersion |= Integer.MIN_VALUE;
    }

    @Inject(method = "set(ILjava/lang/Object;)V", at = @At("RETURN"))
    private void polydium$bumpOnSet(CallbackInfo ci) {
        this.polydium$bumpPaletteVersion();
    }

    @Inject(method = "getAndSet(ILjava/lang/Object;)Ljava/lang/Object;", at = @At("RETURN"))
    private void polydium$bumpOnGetAndSet(CallbackInfoReturnable<Object> cir) {
        this.polydium$bumpPaletteVersion();
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void polydium$bumpOnRead(CallbackInfo ci) {
        this.polydium$bumpPaletteVersion();
    }
}
//?}
