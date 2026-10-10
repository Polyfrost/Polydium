package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK10;
import org.polyfrost.polydium.client.perf.VulkanBufferMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanGpuBuffer.Direct.class)
public abstract class MixinVulkanBufferMapping implements VulkanBufferMapping {
    @Shadow
    @Final
    protected VulkanDevice device;

    @Shadow
    @Final
    private long vmaAllocation;

    @Shadow
    private int mappingRefCount;

    @Unique
    private long polydium$persistentPointer;

    @Override
    public boolean polydium$isPersistentlyMapped() {
        return this.polydium$persistentPointer != 0L;
    }

    @WrapOperation(
            method = "map(JJZZ)Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice$MappedView;",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/vma/Vma;vmaMapMemory(JJLorg/lwjgl/PointerBuffer;)I")
    )
    private int polydium$keepMapped(long allocator, long allocation, PointerBuffer pointer, Operation<Integer> original) {
        boolean onlyView = this.mappingRefCount == 1;
        if (this.polydium$persistentPointer != 0L) {
            pointer.put(0, this.polydium$persistentPointer);
            return VK10.VK_SUCCESS;
        }
        int result = original.call(allocator, allocation, pointer);
        if (result == VK10.VK_SUCCESS && onlyView) {
            this.polydium$persistentPointer = pointer.get(0);
        }
        return result;
    }

    @Inject(method = "destroy()V", at = @At("HEAD"))
    private void polydium$releaseMapping(CallbackInfo ci) {
        if (this.polydium$persistentPointer != 0L) {
            Vma.vmaUnmapMemory(this.device.vma(), this.vmaAllocation);
            this.polydium$persistentPointer = 0L;
        }
    }
}
//?}
