package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.1 {
//? if >= 26.3 {
import com.mojang.blaze3d.pipeline.PipelineCache;
import com.mojang.blaze3d.systems.RenderSystem;
//?} else {
/*import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.backend.api.GpuDeviceBackend;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if >= 26.3 {
@Mixin(RenderSystem.class)
public interface RenderSystemPipelineCacheAccessor {
    @Accessor("currentPipelineCache")
    static PipelineCache polydium$currentPipelineCache() {
        throw new AssertionError();
    }
}
//?} else {
/*@Mixin(GpuDevice.class)
public interface RenderSystemPipelineCacheAccessor {
    @Accessor("backend")
    GpuDeviceBackend polydium$backend();
}
*///?}
//?}
