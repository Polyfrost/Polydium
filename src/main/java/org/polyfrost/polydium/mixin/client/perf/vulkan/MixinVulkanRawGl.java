package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import org.polyfrost.polydium.client.perf.RawGl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanDevice.class)
public abstract class MixinVulkanRawGl {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void polydium$stubRawGl(CallbackInfo ci) {
        var info = ((VulkanDevice) (Object) this).getDeviceInfo();
        RawGl.install(info.vendorName(), info.name(), info.driverInfo(), info.limits().maxTextureSize());
    }
}
//?}
