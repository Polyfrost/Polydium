package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import java.nio.LongBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocationInfo;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.polyfrost.polydium.client.perf.VmaPolicy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VulkanGpuBuffer.Direct.class)
public abstract class MixinVmaBufferPolicy {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/vma/Vma;vmaCreateBuffer(JLorg/lwjgl/vulkan/VkBufferCreateInfo;Lorg/lwjgl/util/vma/VmaAllocationCreateInfo;Ljava/nio/LongBuffer;Lorg/lwjgl/PointerBuffer;Lorg/lwjgl/util/vma/VmaAllocationInfo;)I"))
    private static int polydium$dedicatedLargeBuffers(long allocator, VkBufferCreateInfo bufferInfo, VmaAllocationCreateInfo allocInfo, LongBuffer buffer, PointerBuffer allocation, VmaAllocationInfo info, Operation<Integer> original) {
        return VmaPolicy.create(allocator, bufferInfo.size(), allocInfo.flags(), flags -> {
            allocInfo.flags(flags);
            return original.call(allocator, bufferInfo, allocInfo, buffer, allocation, info);
        });
    }
}
//?}
