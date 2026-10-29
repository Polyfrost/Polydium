package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import org.polyfrost.polydium.client.perf.VertexCopy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanCommandEncoder.class)
public abstract class MixinVulkanVertexCopyFinish {
    @Inject(method = "submit", at = @At("HEAD"))
    private void polydium$finishVertexCopies(CallbackInfo ci) {
        VertexCopy.finishInFlight();
    }
}
//?}
