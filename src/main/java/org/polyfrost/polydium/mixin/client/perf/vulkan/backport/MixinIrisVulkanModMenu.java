package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import org.polyfrost.polydium.vulkan.IrisVulkan;
import org.polyfrost.polydium.vulkan.IrisVulkanGate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.compat.modmenu.ModMenuIntegration", remap = false)
public class MixinIrisVulkanModMenu {

    @Inject(method = "getModConfigScreenFactory", at = @At("HEAD"), cancellable = true)
    private void polydium$vulkan(CallbackInfoReturnable<Object> cir) throws ClassNotFoundException {
        if (IrisVulkanGate.applied)
            cir.setReturnValue(IrisVulkan.modMenuFactory(getClass().getClassLoader()));
    }
}
