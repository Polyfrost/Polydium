package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
//? if >= 1.21.11 {
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
//?}
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TextureSetup.class)
public abstract class MixinGuiTextureSetupReuse {

    @Unique
    private static final TextureSetup[] polydium$setups = new TextureSetup[8];
    @Unique
    private static int polydium$lastHit;
    @Unique
    private static int polydium$nextSlot;

    //? if >= 1.21.11 {
    @WrapMethod(method = "singleTextureWithLightmap")
    private static TextureSetup polydium$reuseLastSetup(GpuTextureView texture, GpuSampler sampler, Operation<TextureSetup> original) {

        //? if >= 26.1 {
        GpuTextureView lightmap        = Minecraft.getInstance().gameRenderer.lightmap();
        //?} else {
        /*GpuTextureView lightmap        = Minecraft.getInstance().gameRenderer.lightTexture().getTextureView();
        *///?}
        GpuSampler     lightmapSampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        TextureSetup[] setups          = polydium$setups;
        int            start           = polydium$lastHit;

        for (int i = 0; i < setups.length; i++) {
            int          slot   = (start + i) & (setups.length - 1);
            TextureSetup cached = setups[slot];
            if (cached != null && cached.texure0() == texture && cached.sampler0() == sampler && cached.texure2() == lightmap && cached.sampler2() == lightmapSampler) {
                polydium$lastHit = slot;
                return cached;
            }
        }

        TextureSetup setup = original.call(texture, sampler);

        polydium$store(setup);

        return setup;
    }
    //?} else {
    /*@WrapMethod(method = "singleTextureWithLightmap")
    private static TextureSetup polydium$reuseLastSetup(GpuTextureView texture, Operation<TextureSetup> original) {

        GpuTextureView lightmap = Minecraft.getInstance().gameRenderer.lightTexture().getTextureView();
        TextureSetup[] setups   = polydium$setups;
        int            start    = polydium$lastHit;

        for (int i = 0; i < setups.length; i++) {
            int          slot   = (start + i) & (setups.length - 1);
            TextureSetup cached = setups[slot];
            if (cached != null && cached.texure0() == texture && cached.texure2() == lightmap) {
                polydium$lastHit = slot;
                return cached;
            }
        }

        TextureSetup setup = original.call(texture);

        polydium$store(setup);

        return setup;
    }
    *///?}

    @Unique
    private static void polydium$store(TextureSetup setup) {
        TextureSetup[] setups = polydium$setups;
        int            slot   = polydium$nextSlot;
        setups[slot] = setup;
        polydium$lastHit = slot;
        polydium$nextSlot = (slot + 1) & (setups.length - 1);
    }
}
//?}
