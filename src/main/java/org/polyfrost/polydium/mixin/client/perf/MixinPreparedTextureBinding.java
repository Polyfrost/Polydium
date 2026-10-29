package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.util.TextureViewAndSampler;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import org.polyfrost.polydium.client.perf.TextureList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(PreparedRenderType.class)
public class MixinPreparedTextureBinding {
    @Shadow
    @Final
    private List<PreparedRenderType.Texture> textures;

    @WrapWithCondition(method = "draw", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass;setUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Lcom/mojang/renderpearl/api/textures/GpuSampler;)V"))
    private boolean polydium$bindPreparedTexture(RenderPass pass, String name, GpuTextureView view, GpuSampler sampler) {
        if (!(this.textures instanceof TextureList list) || !(pass instanceof TextureList.Binder binder)) {
            return true;
        }
        TextureViewAndSampler binding = list.binding(name, view, sampler);
        return binding == null || !binder.polydium$bindTexture(name, binding);
    }
}
//?}
