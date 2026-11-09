package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.backend.vulkan.VulkanBackend;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.polyfrost.polydium.client.perf.VmaPolicy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VulkanBackend.class)
public abstract class MixinVmaAllocatorPolicy {
    @WrapOperation(method = "createVma", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/vma/Vma;vmaCreateAllocator(Lorg/lwjgl/util/vma/VmaAllocatorCreateInfo;Lorg/lwjgl/PointerBuffer;)I"))
    private static int polydium$smallerBlocks(VmaAllocatorCreateInfo createInfo, PointerBuffer allocator, Operation<Integer> original) {
        createInfo.preferredLargeHeapBlockSize(VmaPolicy.BLOCK_BYTES);
        return original.call(createInfo, allocator);
    }
}
//?}
