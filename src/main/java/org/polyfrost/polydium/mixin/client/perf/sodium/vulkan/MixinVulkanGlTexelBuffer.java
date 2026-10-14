package org.polyfrost.polydium.mixin.client.perf.sodium.vulkan;

//? if >= 26.1 {
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.opengl.GlStateManager;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlTexelBuffer;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GlTexelBuffer.class, remap = false)
public class MixinVulkanGlTexelBuffer {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_genTexture()I", remap = true))
    private int polydium$noGlTexture() {
        return VulkanActive.active ? 0 : GlStateManager._genTexture();
    }

    @WrapWithCondition(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL46C;glBindTexture(II)V"))
    private boolean polydium$noGlBind(int target, int texture) {
        return !VulkanActive.active;
    }

    @WrapWithCondition(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL46C;glTexBuffer(III)V"))
    private boolean polydium$noGlTexBuffer(int target, int format, int buffer) {
        return !VulkanActive.active;
    }

    @Inject(method = "destroy", at = @At("HEAD"), cancellable = true)
    private void polydium$noGlDelete(CallbackInfo ci) {
        if (VulkanActive.active)
            ci.cancel();
    }
}
//?}
