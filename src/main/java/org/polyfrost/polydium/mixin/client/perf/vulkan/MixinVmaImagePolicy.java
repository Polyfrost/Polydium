package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTexture;
import java.nio.LongBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocationInfo;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.polyfrost.polydium.client.perf.VmaPolicy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VulkanGpuTexture.class)
public abstract class MixinVmaImagePolicy {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/vma/Vma;vmaCreateImage(JLorg/lwjgl/vulkan/VkImageCreateInfo;Lorg/lwjgl/util/vma/VmaAllocationCreateInfo;Ljava/nio/LongBuffer;Lorg/lwjgl/PointerBuffer;Lorg/lwjgl/util/vma/VmaAllocationInfo;)I"))
    private int polydium$dedicatedLargeImages(long allocator, VkImageCreateInfo imageInfo, VmaAllocationCreateInfo allocInfo, LongBuffer image, PointerBuffer allocation, VmaAllocationInfo info, Operation<Integer> original, @Local(argsOnly = true) GpuFormat format) {
        long bytes = VmaPolicy.imageBytes(format.blockSize(), imageInfo.extent().width(), imageInfo.extent().height(), imageInfo.arrayLayers(), imageInfo.mipLevels());
        return VmaPolicy.create(allocator, bytes, allocInfo.flags(), flags -> {
            allocInfo.flags(flags);
            return original.call(allocator, imageInfo, allocInfo, image, allocation, info);
        });
    }
}
//?}
