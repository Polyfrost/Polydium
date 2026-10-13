package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 && < 26.3 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanTransientMemory;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocationInfo;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.polyfrost.polydium.client.perf.TransientHeapPlacement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.LongBuffer;

@Mixin(VulkanTransientMemory.class)
public abstract class MixinTransientHeapPlacement {

    @Shadow
    @Final
    private VulkanDevice device;

    @Shadow
    @Final
    private boolean useDeviceMemoryForMappedGpuStaging;

    @Unique
    private TransientHeapPlacement polydium$placement;

    @Unique
    private int polydium$memoryType = -1;

    @ModifyArg(method = "allocateVulkanBlock", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/vma/VmaAllocationCreateInfo;usage(I)Lorg/lwjgl/util/vma/VmaAllocationCreateInfo;"))
    private int polydium$preferDeviceOnUnifiedMemory(int usage) {
        return this.useDeviceMemoryForMappedGpuStaging ? Vma.VMA_MEMORY_USAGE_AUTO_PREFER_DEVICE : usage;
    }

    @WrapOperation(method = "allocateVulkanBlock", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/vma/Vma;vmaCreateBuffer(JLorg/lwjgl/vulkan/VkBufferCreateInfo;Lorg/lwjgl/util/vma/VmaAllocationCreateInfo;Ljava/nio/LongBuffer;Lorg/lwjgl/PointerBuffer;Lorg/lwjgl/util/vma/VmaAllocationInfo;)I"))
    private int polydium$captureMemoryType(long allocator, VkBufferCreateInfo bufferInfo, VmaAllocationCreateInfo allocationCreateInfo, LongBuffer buffer, PointerBuffer allocation, VmaAllocationInfo ignored, Operation<Integer> original) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VmaAllocationInfo info   = VmaAllocationInfo.calloc(stack);
            int               result = original.call(allocator, bufferInfo, allocationCreateInfo, buffer, allocation, info);
            this.polydium$memoryType = result == VK10.VK_SUCCESS ? info.memoryType() : -1;
            return result;
        }
    }

    @Inject(method = "allocateVulkanBlock", at = @At("RETURN"))
    private void polydium$markSuboptimal(long size, boolean staging, CallbackInfoReturnable<Object> cir) {
        int memoryType = this.polydium$memoryType;
        this.polydium$memoryType = -1;

        if (memoryType == -1)
            return;

        if (this.polydium$placement == null) {
            this.polydium$placement = new TransientHeapPlacement(this.device.vkDevice().getPhysicalDevice(), this.useDeviceMemoryForMappedGpuStaging);
        }

        if (this.polydium$placement.suboptimal(memoryType, staging && !this.useDeviceMemoryForMappedGpuStaging)) {
            TransientHeapPlacement.SUBOPTIMAL.add(cir.getReturnValue());
        }
    }

    @Inject(method = "freeVulkanBlock", at = @At("HEAD"))
    private void polydium$forgetFreedBlock(@Coerce Object block, CallbackInfo ci) {
        TransientHeapPlacement.SUBOPTIMAL.remove(block);
    }
}
*///?}
