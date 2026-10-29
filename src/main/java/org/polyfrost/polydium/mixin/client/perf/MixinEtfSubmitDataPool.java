package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.10 {
//? if >= 26.2 {
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
//?} else
/*import net.minecraft.client.renderer.SubmitNodeStorage;*/
import org.spongepowered.asm.mixin.Mixin;

//? if >= 26.2 {
@Mixin(ModelFeatureRenderer.Submit.class)
//?} else
/*@Mixin(SubmitNodeStorage.ModelSubmit.class)*/
public abstract class MixinEtfSubmitDataPool {
}
//?}
