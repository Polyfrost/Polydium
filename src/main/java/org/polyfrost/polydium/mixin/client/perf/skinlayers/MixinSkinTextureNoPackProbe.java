package org.polyfrost.polydium.mixin.client.perf.skinlayers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
//? if >= 1.21.4 {
import net.minecraft.client.renderer.texture.DynamicTexture;
//?} else {
/*import org.polyfrost.polydium.mixin.client.perf.HttpTextureAccessor;
*///?}
//? if >= 1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
*///?}
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.polyfrost.polydium.mixin.client.perf.TextureManagerAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Pseudo
@Mixin(targets = "dev.tr7zw.skinlayers.SkinUtil", remap = false)
public abstract class MixinSkinTextureNoPackProbe {
    //? if >= 1.21.11 {
    @WrapOperation(method = "getTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ResourceManager;getResource(Lnet/minecraft/resources/Identifier;)Ljava/util/Optional;", remap = true), require = 0, expect = 0)
    private static Optional<Resource> polydium$skipPackProbe(ResourceManager manager, Identifier id, Operation<Optional<Resource>> original) {
    //?} else {
    /*@WrapOperation(method = "getTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ResourceManager;getResource(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;", remap = true), require = 0, expect = 0)
    private static Optional<Resource> polydium$skipPackProbe(ResourceManager manager, ResourceLocation id, Operation<Optional<Resource>> original) {
    *///?}
        //? if >= 1.21.4 {
        // Without pixels (before 1.21.4, the cached skin file) Skin Layers can't read the texture manager's copy, so the pack is the only source left.
        if (((TextureManagerAccessor) Minecraft.getInstance().getTextureManager()).polydium$byPath().get(id) instanceof DynamicTexture texture && texture.getPixels() != null)
        //?} else {
        /*if (((TextureManagerAccessor) Minecraft.getInstance().getTextureManager()).polydium$byPath().get(id) instanceof HttpTextureAccessor texture && texture.polydium$file() != null && texture.polydium$file().isFile())
        *///?}
            return Optional.empty();
        return original.call(manager, id);
    }
}
