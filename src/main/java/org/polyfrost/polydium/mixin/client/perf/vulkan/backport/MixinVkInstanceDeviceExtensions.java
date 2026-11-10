package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

//? if < 26.1 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Backport of LWJGL 3.4.0's fix (LWJGL/lwjgl3@b5b363e2) for drivers whose device extensions don't fit on the default stack
@Mixin(value = VkInstance.class, remap = false)
public class MixinVkInstanceDeviceExtensions {
    @WrapOperation(method = "getAvailableDeviceExtensions", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkExtensionProperties;malloc(ILorg/lwjgl/system/MemoryStack;)Lorg/lwjgl/vulkan/VkExtensionProperties$Buffer;"))
    private static VkExtensionProperties.Buffer polydium$heapIfTooLarge(int capacity, MemoryStack stack, Operation<VkExtensionProperties.Buffer> original) {
        if ((long) capacity * VkExtensionProperties.SIZEOF > stack.getPointer()) {
            return VkExtensionProperties.create(capacity);
        }
        return original.call(capacity, stack);
    }
}
*///?}
