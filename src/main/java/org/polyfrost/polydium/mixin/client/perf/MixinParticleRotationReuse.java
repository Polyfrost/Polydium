package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.particle.SingleQuadParticle;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SingleQuadParticle.class)
public class MixinParticleRotationReuse {
    @Unique
    private static final Quaternionf polydium$ROTATION = new Quaternionf();

    @WrapOperation(
            //? if >= 26.1 {
            method = "extract(Lnet/minecraft/client/renderer/state/level/QuadParticleRenderState;Lnet/minecraft/client/Camera;F)V",
            //?} else {
            /*method = "extract(Lnet/minecraft/client/renderer/state/QuadParticleRenderState;Lnet/minecraft/client/Camera;F)V",
            *///?}
            at = @At(value = "NEW", target = "()Lorg/joml/Quaternionf;")
    )
    private Quaternionf polydium$reuseRotation(Operation<Quaternionf> original) {
        return polydium$ROTATION.identity();
    }
}
//?}
