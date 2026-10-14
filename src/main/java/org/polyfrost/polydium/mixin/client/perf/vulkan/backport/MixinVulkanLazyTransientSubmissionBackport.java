package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.polyfrost.polydium.client.perf.VulkanFrontSubmission;
import org.polyfrost.polydium.vulkan.VulkanQueue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VulkanQueue.Submission.class)
public abstract class MixinVulkanLazyTransientSubmissionBackport implements VulkanFrontSubmission {
    @Shadow
    private boolean closed;

    @Shadow
    @Final
    private ReferenceArrayList<Object> stages;

    @Override
    public void polydium$executeFirst(VkCommandBuffer commandBuffer, VulkanQueue queue) {
        if (this.closed) {
            throw new IllegalStateException("Attempt to use closed Submission");
        }

        MixinVulkanLazyTransientStageAccessorBackport first = (MixinVulkanLazyTransientStageAccessorBackport) this.stages.get(0);
        if (first.polydium$waits().isEmpty()) {
            first.polydium$commandBuffers().add(0, commandBuffer);
            return;
        }

        Object stage = ((VulkanFrontSubmission) queue.beginSubmit()).polydium$firstStage();
        ((MixinVulkanLazyTransientStageAccessorBackport) stage).polydium$commandBuffers().add(commandBuffer);
        this.stages.add(0, stage);
    }

    @Override
    public Object polydium$firstStage() {
        return this.stages.get(0);
    }
}
