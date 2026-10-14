package org.polyfrost.polydium.mixin.client.perf.sodium.vulkan;

import net.caffeinemc.mods.sodium.api.config.ConfigState;
import net.caffeinemc.mods.sodium.client.gui.SodiumConfigBuilder;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SodiumConfigBuilder.class, remap = false)
public class MixinVulkanSodiumOptions {

    @Inject(method = {
        "lambda$buildNoErrorContextOption$2(Lnet/caffeinemc/mods/sodium/api/config/ConfigState;)Ljava/lang/Boolean;",
        "lambda$buildNoErrorContextOption$41(Lnet/caffeinemc/mods/sodium/api/config/ConfigState;)Ljava/lang/Boolean;",
        "lambda$buildNoErrorContextOption$49(Lnet/caffeinemc/mods/sodium/api/config/ConfigState;)Ljava/lang/Boolean;"
    }, at = @At("HEAD"), cancellable = true, require = 1)
    private static void polydium$noGlContextOption(ConfigState state, CallbackInfoReturnable<Boolean> cir) {
        if (VulkanActive.active)
            cir.setReturnValue(false);
    }
}
