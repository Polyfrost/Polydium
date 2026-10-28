package org.polyfrost.polydium.mixin.client.perf.particle_core;

//? if >= 1.21.10 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.culling.Frustum;
//? if >= 26.2 {
import net.minecraft.client.renderer.extract.LevelExtractor;
//?} else
/*import net.minecraft.client.renderer.LevelRenderer;*/
//? if >= 26.1 {
import net.minecraft.client.renderer.state.level.ParticlesRenderState;
//?} else
/*import net.minecraft.client.renderer.state.ParticlesRenderState;*/
import org.polyfrost.polydium.client.perf.ParticleTickOverlap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? if >= 26.2 {
@Mixin(LevelExtractor.class)
//?} else
/*@Mixin(LevelRenderer.class)*/
public abstract class MixinParticleExtractDefer {
    //? if >= 26.2 {
    @WrapOperation(method = "extract", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;extract(Lnet/minecraft/client/renderer/state/level/ParticlesRenderState;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/Camera;F)V"))
    //?} else if >= 26.1 {
    /*@WrapOperation(method = "extractLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;extract(Lnet/minecraft/client/renderer/state/level/ParticlesRenderState;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/Camera;F)V"))
    *///?} else
    /*@WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;extract(Lnet/minecraft/client/renderer/state/ParticlesRenderState;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/Camera;F)V"))*/
    private void polydium$deferParticleExtract(ParticleEngine engine, ParticlesRenderState state, Frustum frustum, Camera camera, float partialTick, Operation<Void> original) {
        if (!ParticleTickOverlap.deferExtract(engine, state, frustum, camera, partialTick))
            original.call(engine, state, frustum, camera, partialTick);
    }
}
//?}
