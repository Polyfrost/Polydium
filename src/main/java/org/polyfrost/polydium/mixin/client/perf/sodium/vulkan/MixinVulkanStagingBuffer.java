package org.polyfrost.polydium.mixin.client.perf.sodium.vulkan;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.caffeinemc.mods.sodium.client.gl.arena.staging.StagingBuffer;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegionManager;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.polyfrost.polydium.vulkan.sodium.VkStagingBuffer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = RenderRegionManager.class, remap = false)
public class MixinVulkanStagingBuffer {

    @WrapMethod(method = "createStagingBuffer")
    private static StagingBuffer polydium$vulkanStaging(CommandList commandList, Operation<StagingBuffer> original) {
        return VulkanActive.active ? new VkStagingBuffer() : original.call(commandList);
    }
}
