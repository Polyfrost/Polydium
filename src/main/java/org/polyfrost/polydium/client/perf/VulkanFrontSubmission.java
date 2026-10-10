package org.polyfrost.polydium.client.perf;

//? if >= 26.2 {
import com.mojang.renderpearl.backend.vulkan.VulkanQueue;
import org.lwjgl.vulkan.VkCommandBuffer;

public interface VulkanFrontSubmission {
    void polydium$executeFirst(VkCommandBuffer commandBuffer, VulkanQueue queue);

    Object polydium$firstStage();
}
//?}
