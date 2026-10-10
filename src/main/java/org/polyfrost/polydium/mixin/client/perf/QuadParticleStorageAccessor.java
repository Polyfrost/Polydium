package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if >= 26.1 {
@Mixin(targets = "net.minecraft.client.renderer.state.level.QuadParticleRenderState$Storage")
//?} else {
/*@Mixin(targets = "net.minecraft.client.renderer.state.QuadParticleRenderState$Storage")
*///?}
public interface QuadParticleStorageAccessor {
    @Accessor("floatValues")
    float[] polydium$floatValues();

    @Accessor("intValues")
    int[] polydium$intValues();

    @Accessor("currentParticleIndex")
    int polydium$particleCount();
}
//?}
