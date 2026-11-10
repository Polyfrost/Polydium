package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.render.GuiRenderer;
import org.polyfrost.polydium.vulkan.OwoVulkan;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GuiRenderer.class, priority = 1500)
public class MixinVulkanOwoBlur {

    @Shadow
    @Final
    private List<?> draws;

    @WrapMethod(method = "executeDrawRange")
    private void polydium$splitAtBlur(
        Supplier<String> label, RenderTarget mainRenderTarget, GpuBufferSlice fogBuffer, GpuBufferSlice dynamicTransforms, GpuBuffer indexBuffer,
        VertexFormat.IndexType indexType, int startIndex, int endIndex, Operation<Void> original
    ) {
        if (!VulkanActive.active) {
            original.call(label, mainRenderTarget, fogBuffer, dynamicTransforms, indexBuffer, indexType, startIndex, endIndex);
            return;
        }

        int segStart = startIndex;
        for (int i = startIndex; i < endIndex; i++) {
            if (!OwoVulkan.isBlur(this.draws.get(i))) continue;

            if (i > segStart) {
                original.call(label, mainRenderTarget, fogBuffer, dynamicTransforms, indexBuffer, indexType, segStart, i);
            }

            RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(
                mainRenderTarget.getColorTexture(), OwoVulkan.blurInput().getColorTexture(), 0, 0, 0, 0, 0, mainRenderTarget.width, mainRenderTarget.height
            );
            original.call(label, mainRenderTarget, fogBuffer, dynamicTransforms, indexBuffer, indexType, i, i + 1);
            segStart = i + 1;
        }

        if (segStart < endIndex) {
            original.call(label, mainRenderTarget, fogBuffer, dynamicTransforms, indexBuffer, indexType, segStart, endIndex);
        }
    }
}
