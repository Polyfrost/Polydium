package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 && < 26.3 {
/*import com.mojang.blaze3d.vulkan.VulkanBindGroupLayout;
import org.polyfrost.polydium.client.perf.VulkanDynamicState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(VulkanBindGroupLayout.class)
public abstract class MixinVulkanBindGroupLayoutSlots implements VulkanDynamicState.LayoutSlots {
    @Shadow
    public abstract List<VulkanBindGroupLayout.Entry> entries();

    @Unique
    private int[] polydium$slots;

    @Override
    public int[] polydium$slots() {
        int[] slots = this.polydium$slots;
        if (slots == null) {
            slots = this.polydium$slots = VulkanDynamicState.slots(this.entries());
        }
        return slots;
    }
}
*///?}
