package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
//? if >= 26.2 {
import com.mojang.blaze3d.systems.SamplerCache;
//?} else {
/*import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
*///?}
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
//? if >= 26.2
import org.polyfrost.polydium.client.perf.TextureList;
import org.polyfrost.polydium.client.perf.TextureManagerVersion;
import java.util.List;
import java.util.Map;
//? if >= 26.2
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RenderSetup.class)
public abstract class MixinRenderTypeTextures {

    @Shadow
    @Final
    Map<String, Object> textures;

    @Shadow
    @Final
    boolean useLightmap;

    @Shadow
    @Final
    boolean useOverlay;

    @Unique
    private /*? if >= 26.2 {*/TextureList/*?} else {*//*Map<String, RenderSetup.TextureAndSampler>*//*?}*/ polydium$prepared;

    @Unique
    private String[] polydium$names;

    @Unique
    private RenderSetupBindingAccessor[] polydium$bindings;

    @Unique
    private AbstractTexture[] polydium$textures;

    @Unique
    private TextureManager polydium$texturesOwner;

    @Unique
    private int polydium$texturesVersion;

    //? if >= 26.2 {
    @WrapMethod(method = "prepareTextures")
    private List<PreparedRenderType.Texture> polydium$reuseTextures(TextureManager textureManager, SamplerCache samplerCache, GpuTextureView overlayTexture, GpuTextureView lightmapTexture, Operation<List<PreparedRenderType.Texture>> original) {

        if (this.textures.isEmpty() && !this.useOverlay && !this.useLightmap)
            return original.call(textureManager, samplerCache, overlayTexture, lightmapTexture);

        if (this.polydium$names == null)
            this.polydium$readBindings();

        TextureList prepared = this.polydium$prepared;

        if (prepared != null && this.polydium$matches(prepared, textureManager, samplerCache, overlayTexture, lightmapTexture))
            return prepared;

        prepared               = new TextureList(original.call(textureManager, samplerCache, overlayTexture, lightmapTexture).toArray(PreparedRenderType.Texture[]::new));
        this.polydium$prepared = prepared;

        return prepared;
    }

    //?}

    @Unique
    private void polydium$readBindings() {
        String[]                     names    = new String[this.textures.size()];
        RenderSetupBindingAccessor[] bindings = new RenderSetupBindingAccessor[names.length];
        int                          index    = 0;

        for (Map.Entry<String, Object> entry : this.textures.entrySet()) {
            names[index]    = entry.getKey();
            bindings[index] = (RenderSetupBindingAccessor) entry.getValue();
            index ++;
        }

        this.polydium$bindings = bindings;
        this.polydium$names    = names;
    }

    @Unique
    private AbstractTexture[] polydium$resolve(TextureManager textureManager) {
        TextureManagerVersion version  = (TextureManagerVersion) textureManager;
        AbstractTexture[]     textures = this.polydium$textures;

        if (textures != null && this.polydium$texturesOwner == textureManager && this.polydium$texturesVersion == version.polydium$getVersion())
            return textures;

        textures = new AbstractTexture[this.polydium$names.length];

        for (int binding = 0; binding < textures.length; binding ++)
            textures[binding] = textureManager.getTexture(this.polydium$bindings[binding].polydium$location());

        this.polydium$textures        = textures;
        this.polydium$texturesOwner   = textureManager;
        this.polydium$texturesVersion = version.polydium$getVersion();

        return textures;
    }

    //? if >= 26.2 {
    @Unique
    private boolean polydium$matches(TextureList prepared, TextureManager textureManager, SamplerCache samplerCache, GpuTextureView overlayTexture, GpuTextureView lightmapTexture) {
        if (prepared.size() != this.polydium$names.length + (this.useOverlay ? 1 : 0) + (this.useLightmap ? 1 : 0))
            return false;

        int index = 0;

        if (this.useOverlay && !polydium$same(prepared.get(index ++), "Sampler1", overlayTexture, samplerCache.getClampToEdge(FilterMode.LINEAR)))
            return false;

        if (this.useLightmap && !polydium$same(prepared.get(index ++), "Sampler2", lightmapTexture, samplerCache.getClampToEdge(FilterMode.LINEAR)))
            return false;

        AbstractTexture[] textures = this.polydium$resolve(textureManager);

        for (int binding = 0; binding < this.polydium$names.length; binding ++) {
            AbstractTexture texture         = textures[binding];
            GpuSampler      samplerOverride = this.polydium$bindings[binding].polydium$sampler().get();

            if (!polydium$same(prepared.get(index ++), this.polydium$names[binding], texture.getTextureView(), samplerOverride != null ? samplerOverride : texture.getSampler()))
                return false;
        }

        return true;
    }

    @Unique
    private static boolean polydium$same(PreparedRenderType.Texture texture, String name, GpuTextureView textureView, GpuSampler sampler) {
        return texture.name().equals(name) && texture.textureView() == textureView && texture.sampler() == sampler;
    }
    //?} else {
    /*@WrapMethod(method = "getTextures")
    private Map<String, RenderSetup.TextureAndSampler> polydium$reuseTextures(Operation<Map<String, RenderSetup.TextureAndSampler>> original) {
        if (this.textures.isEmpty() && !this.useOverlay && !this.useLightmap)
            return original.call();

        if (this.polydium$names == null)
            this.polydium$readBindings();

        Map<String, RenderSetup.TextureAndSampler> prepared = this.polydium$prepared;

        if (prepared != null && this.polydium$matches(prepared))
            return prepared;

        prepared               = original.call();
        this.polydium$prepared = prepared;

        return prepared;
    }

    @Unique
    private boolean polydium$matches(Map<String, RenderSetup.TextureAndSampler> prepared) {
        Minecraft  minecraft = Minecraft.getInstance();
        GpuSampler clamp     = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);

        if (this.useOverlay && !polydium$same(prepared.get("Sampler1"), minecraft.gameRenderer.overlayTexture().getTextureView(), clamp))
            return false;

        if (this.useLightmap && !polydium$same(prepared.get("Sampler2"), minecraft.gameRenderer./^? if >= 26.1 {^/lightmap()/^?} else {^//^lightTexture().getTextureView()^//^?}^/, clamp))
            return false;

        AbstractTexture[] textures = this.polydium$resolve(minecraft.getTextureManager());

        for (int binding = 0; binding < this.polydium$names.length; binding ++) {
            AbstractTexture texture         = textures[binding];
            GpuSampler      samplerOverride = this.polydium$bindings[binding].polydium$sampler().get();

            if (!polydium$same(prepared.get(this.polydium$names[binding]), texture.getTextureView(), samplerOverride != null ? samplerOverride : texture.getSampler()))
                return false;
        }

        return true;
    }

    @Unique
    private static boolean polydium$same(RenderSetup.TextureAndSampler texture, GpuTextureView textureView, GpuSampler sampler) {
        return texture.textureView() == textureView && texture.sampler() == sampler;
    }
    *///?}
}
//?}
