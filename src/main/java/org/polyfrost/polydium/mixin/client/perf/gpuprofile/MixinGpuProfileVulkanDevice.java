package org.polyfrost.polydium.mixin.client.perf.gpuprofile;

//? if >= 26.2 {
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import org.polyfrost.polydium.client.perf.gpuprofile.GpuProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanDevice.class)
public abstract class MixinGpuProfileVulkanDevice {
    @Inject(method = "close", at = @At("HEAD"))
    private void polydium$closeProfilerStamps(CallbackInfo ci) {
        GpuProfiler.shutdown();
    }
}
//?}
