package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.mojang.renderpearl.backend.vulkan.Destroyable;
import com.mojang.renderpearl.backend.vulkan.DestructionQueue;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import org.polyfrost.polydium.client.perf.BackgroundDestroyer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(VulkanCommandEncoder.class)
public abstract class MixinVulkanBackgroundDestroy {
    @SuppressWarnings("unchecked")
    @ModifyArg(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/vulkan/DestructionQueue;<init>(ILcom/mojang/renderpearl/backend/vulkan/DestructionQueue$Destroyer;)V"),
        index = 1
    )
    private DestructionQueue.Destroyer<?> polydium$destroyInBackground(DestructionQueue.Destroyer<?> destroyer) {
        return new BackgroundDestroyer((DestructionQueue.Destroyer<Destroyable>) destroyer);
    }
}
//?}
