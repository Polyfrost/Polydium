package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import java.util.OptionalInt;
import net.minecraft.client.renderer.RenderPipelines;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.kir.cubeswithoutborders.client.util.FramebufferUtil", remap = false)
public class MixinVulkanCubesWithoutBorders {
    // replaces a raw GL framebuffer blit that casts attachments to GlTexture
    @Inject(method = "draw", at = @At("HEAD"), cancellable = true, require = 0)
    private static void polydium$blit(RenderTarget source, RenderTarget window, CallbackInfo ci) {
        if (!VulkanActive.active) return;
        ci.cancel();
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Cubes Without Borders blit", window.getColorTextureView(), OptionalInt.empty())) {
            pass.setPipeline(RenderPipelines.TRACY_BLIT);
            RenderSystem.bindDefaultUniforms(pass);
            pass.bindTexture("InSampler", source.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            pass.draw(0, 3);
        }
    }
}
