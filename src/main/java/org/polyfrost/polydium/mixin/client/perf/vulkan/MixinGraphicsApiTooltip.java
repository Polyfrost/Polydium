package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import org.polyfrost.polydium.client.VulkanAvailability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Options.class)
public abstract class MixinGraphicsApiTooltip {
    // Graphics API option tooltip: says why when Vulkan can't start here
    @ModifyExpressionValue(method = "lambda$new$8", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;GRAPHICS_API_TOOLTIP_VULKAN:Lnet/minecraft/network/chat/Component;"))
    private Component polydium$explainUnavailable(Component tooltip) {
        return VulkanAvailability.vulkanTooltip();
    }
}
//?}
