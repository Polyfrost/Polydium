package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.polyfrost.polydium.vulkan.IrisVulkan;
import org.polyfrost.polydium.vulkan.IrisVulkanGate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Coerce;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.compat.modmenu.ModMenuIntegration", remap = false)
public class MixinIrisVulkanModMenu {

    @Coerce
    @WrapMethod(method = "getModConfigScreenFactory")
    private Object polydium$vulkan(Operation<Object> original) throws ClassNotFoundException {
        return IrisVulkanGate.applied ? IrisVulkan.modMenuFactory(getClass().getClassLoader()) : original.call();
    }
}
