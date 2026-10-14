package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
//? if >= 26.2 {
import com.mojang.renderpearl.backend.vulkan.VulkanQueue;
//?} else
/*import org.polyfrost.polydium.vulkan.VulkanQueue;*/
import org.lwjgl.vulkan.VkCommandBuffer;

public interface VulkanFrontSubmission {
    void polydium$executeFirst(VkCommandBuffer commandBuffer, VulkanQueue queue);

    Object polydium$firstStage();
}
//?}
