package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.polyfrost.polydium.client.perf.ParticleAirCollision;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    //? if >= 1.21.10 {
    @ModifyExpressionValue(method = {"set(ILjava/lang/Object;)V", "getAndSet(ILjava/lang/Object;)Ljava/lang/Object;"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/Palette;idFor(Ljava/lang/Object;Lnet/minecraft/world/level/chunk/PaletteResize;)I"))
    //?} else
    //@ModifyExpressionValue(method = {"set(ILjava/lang/Object;)V", "getAndSet(ILjava/lang/Object;)Ljava/lang/Object;"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/Palette;idFor(Ljava/lang/Object;)I"))
    private int polydium$bumpOnPaletteWrite(int id) {
        this.polydium$paletteVersion++;
        return id;
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void polydium$bumpOnRead(CallbackInfo ci) {
        this.polydium$paletteVersion++;
    }
}
//?}
