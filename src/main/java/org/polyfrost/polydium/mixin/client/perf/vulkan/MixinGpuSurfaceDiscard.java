package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 1.21.11 {
//? if >= 26.3 {
import com.mojang.renderpearl.backend.api.GpuSurfaceBackend;
import com.mojang.renderpearl.frontend.FrontendGpuSurface;
//?} else if >= 26.2 {
/*import com.mojang.renderpearl.api.device.GpuSurface;
import com.mojang.renderpearl.api.device.GpuSurfaceBackend;
*///?} else {
/*import org.polyfrost.polydium.vulkan.compat.GpuSurface;
import org.polyfrost.polydium.vulkan.compat.GpuSurfaceBackend;
*///?}
import org.polyfrost.polydium.client.perf.PresentThread;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

//? if >= 26.3 {
@Mixin(FrontendGpuSurface.class)
//?} else {
/*@Mixin(GpuSurface.class)
*///?}
public abstract class MixinGpuSurfaceDiscard implements PresentThread.Discard {
    @Shadow
    @Final
    private GpuSurfaceBackend backend;

    @Shadow
    private boolean hasImageAcquired;

    @Shadow
    private boolean hasBlittedTexture;

    @Override
    public void polydium$discardAcquired() {
        if (!this.hasImageAcquired || this.hasBlittedTexture)
            throw new IllegalStateException("Only an acquired image that nothing was copied to can be discarded");

        ((PresentThread.Discard) this.backend).polydium$discardAcquired();
        this.hasImageAcquired = false;
    }
}
//?}
