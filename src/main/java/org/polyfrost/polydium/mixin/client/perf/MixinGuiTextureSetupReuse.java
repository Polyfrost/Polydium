package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TextureSetup.class)
public abstract class MixinGuiTextureSetupReuse {

    @Unique
    private static TextureSetup polydium$last;

    @WrapMethod(method = "singleTextureWithLightmap")
    private static TextureSetup polydium$reuseLastSetup(GpuTextureView texture, GpuSampler sampler, Operation<TextureSetup> original) {

        //? if >= 26.1 {
        GpuTextureView lightmap        = Minecraft.getInstance().gameRenderer.lightmap();
        //?} else {
        /*GpuTextureView lightmap        = Minecraft.getInstance().gameRenderer.lightTexture().getTextureView();
        *///?}
        GpuSampler     lightmapSampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        TextureSetup   last            = polydium$last;

        if (last != null && last.texure0() == texture && last.sampler0() == sampler && last.texure2() == lightmap && last.sampler2() == lightmapSampler)
            return last;

        TextureSetup setup = original.call(texture, sampler);

        polydium$last = setup;

        return setup;
    }
}
//?}
