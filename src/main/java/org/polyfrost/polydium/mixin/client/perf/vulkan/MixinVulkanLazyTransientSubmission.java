package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.mojang.renderpearl.backend.vulkan.VulkanQueue;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.polyfrost.polydium.client.perf.VulkanFrontSubmission;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VulkanQueue.Submission.class)
public abstract class MixinVulkanLazyTransientSubmission implements VulkanFrontSubmission {
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

        MixinVulkanLazyTransientStageAccessor first = (MixinVulkanLazyTransientStageAccessor) this.stages.get(0);

        if (first.polydium$waits().isEmpty()) {
            first.polydium$commandBuffers().add(0, commandBuffer);
            return;
        }

        Object stage = ((VulkanFrontSubmission) queue.beginSubmit()).polydium$firstStage();

        ((MixinVulkanLazyTransientStageAccessor) stage).polydium$commandBuffers().add(commandBuffer);
        this.stages.add(0, stage);
    }

    @Override
    public Object polydium$firstStage() {
        return this.stages.get(0);
    }
}
//?}
