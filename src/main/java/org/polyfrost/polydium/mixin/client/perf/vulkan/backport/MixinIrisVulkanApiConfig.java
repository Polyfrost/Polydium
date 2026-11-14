package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.polyfrost.polydium.vulkan.IrisVulkanGate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

// Iris never initialises its config on Vulkan, so its API would NPE for other mods
@Pseudo
@Mixin(targets = "net.irisshaders.iris.apiimpl.IrisApiV0ConfigImpl", remap = false)
public class MixinIrisVulkanApiConfig {

    @WrapMethod(method = "areShadersEnabled")
    private boolean polydium$vulkan(Operation<Boolean> original) {
        return !IrisVulkanGate.applied && original.call();
    }

    @WrapMethod(method = "setShadersEnabledAndApply")
    private void polydium$vulkan(boolean enabled, Operation<Void> original) {
        if (!IrisVulkanGate.applied)
            original.call(enabled);
    }
}
