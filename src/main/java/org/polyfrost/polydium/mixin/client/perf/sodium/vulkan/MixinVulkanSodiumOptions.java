package org.polyfrost.polydium.mixin.client.perf.sodium.vulkan;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.caffeinemc.mods.sodium.api.config.ConfigState;
import net.caffeinemc.mods.sodium.client.gui.SodiumConfigBuilder;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = SodiumConfigBuilder.class, remap = false)
public class MixinVulkanSodiumOptions {

    @WrapMethod(method = {
        "lambda$buildNoErrorContextOption$2(Lnet/caffeinemc/mods/sodium/api/config/ConfigState;)Ljava/lang/Boolean;",
        "lambda$buildNoErrorContextOption$41(Lnet/caffeinemc/mods/sodium/api/config/ConfigState;)Ljava/lang/Boolean;",
        "lambda$buildNoErrorContextOption$49(Lnet/caffeinemc/mods/sodium/api/config/ConfigState;)Ljava/lang/Boolean;"
    }, require = 1)
    private static Boolean polydium$noGlContextOption(ConfigState state, Operation<Boolean> original) {
        return VulkanActive.active ? Boolean.FALSE : original.call(state);
    }
}
