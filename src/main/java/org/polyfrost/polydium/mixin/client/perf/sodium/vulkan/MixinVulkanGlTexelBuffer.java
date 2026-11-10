package org.polyfrost.polydium.mixin.client.perf.sodium.vulkan;

//? if >= 26.1 {
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlTexelBuffer;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = GlTexelBuffer.class, remap = false)
public class MixinVulkanGlTexelBuffer {

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_genTexture()I", remap = true))
    private int polydium$noGlTexture(Operation<Integer> original) {
        return VulkanActive.active ? 0 : original.call();
    }

    @WrapWithCondition(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL46C;glBindTexture(II)V"))
    private boolean polydium$noGlBind(int target, int texture) {
        return !VulkanActive.active;
    }

    @WrapWithCondition(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL46C;glTexBuffer(III)V"))
    private boolean polydium$noGlTexBuffer(int target, int format, int buffer) {
        return !VulkanActive.active;
    }

    @WrapWithCondition(method = "destroy", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_deleteTexture(I)V", remap = true))
    private boolean polydium$noGlDelete(int texture) {
        return !VulkanActive.active;
    }
}
//?}
