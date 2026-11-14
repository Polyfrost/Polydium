package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import org.polyfrost.polydium.vulkan.VulkanDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.moulberry.flashback.exporting.ExportJob", remap = false)
public abstract class MixinVulkanBackendListFlashbackExport {
    @Inject(method = "finishFrame", at = @At("HEAD"), remap = false, require = 0)
    private void polydium$closeLeakedPassBeforeExportPresent(CallbackInfoReturnable<Boolean> cir) {
        if (VulkanDevice.active != null)
            VulkanDevice.active.createCommandEncoder().closeLeakedRenderPass();
    }
}
