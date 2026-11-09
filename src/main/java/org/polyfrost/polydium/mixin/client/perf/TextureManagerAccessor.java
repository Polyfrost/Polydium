package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
//? if >= 1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(TextureManager.class)
public interface TextureManagerAccessor {
    @Accessor("byPath")
    //? if >= 1.21.11 {
    Map<Identifier, AbstractTexture> polydium$byPath();
    //?} else {
    /*Map<ResourceLocation, AbstractTexture> polydium$byPath();
    *///?}
}
