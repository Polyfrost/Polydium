package org.polyfrost.polydium.mixin.client.perf.sodium.vulkan;

import net.minecraft.client.renderer.LevelRenderer;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.polyfrost.polydium.vulkan.sodium.SodiumVulkan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinVulkanFrameEnd {

    @Inject(method = "endFrame", at = @At("RETURN"))
    private void polydium$rotateDrawContext(CallbackInfo ci) {
        if (VulkanActive.active)
            SodiumVulkan.endFrame();
    }
}
