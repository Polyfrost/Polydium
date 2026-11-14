package org.polyfrost.polydium.mixin.client.perf.gpuprofile;

//? if >= 26.2 {
import net.minecraft.client.Minecraft;
//?} else {
/*import net.minecraft.client.renderer.GameRenderer;
*///?}
import org.polyfrost.polydium.client.perf.gpuprofile.GpuProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >= 26.2 {
@Mixin(Minecraft.class)
//?} else {
/*@Mixin(GameRenderer.class)
*///?}
public abstract class MixinGpuProfileFrame {
    //? if >= 26.2 {
    @Inject(method = "renderFrame", at = @At("HEAD"))
    //?} else {
    /*@Inject(method = "render", at = @At("HEAD"))
    *///?}
    private void polydium$advanceGpuFrame(CallbackInfo ci) {
        GpuProfiler.frame();
    }
}
