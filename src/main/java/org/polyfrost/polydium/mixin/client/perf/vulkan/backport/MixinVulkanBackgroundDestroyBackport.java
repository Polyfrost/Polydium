package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import org.polyfrost.polydium.client.perf.BackgroundDestroyer;
import org.polyfrost.polydium.vulkan.Destroyable;
import org.polyfrost.polydium.vulkan.DestructionQueue;
import org.polyfrost.polydium.vulkan.VulkanCommandEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(VulkanCommandEncoder.class)
public abstract class MixinVulkanBackgroundDestroyBackport {
    @SuppressWarnings("unchecked")
    @ModifyArg(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lorg/polyfrost/polydium/vulkan/DestructionQueue;<init>(ILorg/polyfrost/polydium/vulkan/DestructionQueue$Destroyer;)V"),
        index = 1
    )
    private DestructionQueue.Destroyer<?> polydium$destroyInBackground(DestructionQueue.Destroyer<?> destroyer) {
        return new BackgroundDestroyer((DestructionQueue.Destroyer<Destroyable>) destroyer);
    }
}
