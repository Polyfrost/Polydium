package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import org.polyfrost.polydium.client.perf.VulkanBufferMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer$Direct$1")
public abstract class MixinVulkanBufferMappingUnmap {
    @Shadow
    @Final
    VulkanGpuBuffer.Direct this$0;

    @WrapWithCondition(
            method = "run()V",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/vma/Vma;vmaUnmapMemory(JJ)V")
    )
    private boolean polydium$keepPersistentMapping(long allocator, long allocation) {
        return !((VulkanBufferMapping) this.this$0).polydium$isPersistentlyMapped();
    }
}
//?}
