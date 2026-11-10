package org.polyfrost.polydium.mixin.client.perf.sodium.vulkan;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.gl.device.GLRenderDevice;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.polyfrost.polydium.vulkan.sodium.VkCommandList;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = GLRenderDevice.class, remap = false)
public class MixinVulkanRenderDevice {

    @WrapMethod(method = "createCommandList")
    private CommandList polydium$vulkanCommandList(Operation<CommandList> original) {
        return VulkanActive.active ? VkCommandList.INSTANCE : original.call();
    }
}
