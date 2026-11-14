package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VertexFormat.Builder.class)
public abstract class MixinVulkanBackendListVertexAlign {
    @Shadow
    private int offset;

    @Inject(method = "add", at = @At("HEAD"))
    private void polydium$alignLineWidth(String name, VertexFormatElement element, CallbackInfoReturnable<VertexFormat.Builder> cir) {
        if (element == VertexFormatElement.LINE_WIDTH)
            this.offset = (this.offset + 3) & ~3;
    }
}
