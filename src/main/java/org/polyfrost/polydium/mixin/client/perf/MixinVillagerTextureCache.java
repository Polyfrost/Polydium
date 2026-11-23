package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
//? if >= 1.21.11 {
import net.minecraft.resources.Identifier;
//?} else
/*import net.minecraft.resources.ResourceLocation;*/
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

@Mixin(VillagerProfessionLayer.class)
public abstract class MixinVillagerTextureCache {
    @Shadow
    @Final
    private String path;

    //? if >= 1.21.11 {
    @Unique
    private final Map<String, Map<Identifier, Identifier>> polydium$textures = new HashMap<>();
    //?} else {
    /*@Unique
    private final Map<String, Map<ResourceLocation, ResourceLocation>> polydium$textures = new HashMap<>();
    *///?}

    /**
     * @author Wyvest
     * @reason Optimize
     */
    @SuppressWarnings("checkstyle:NoOverwrite") // TODO: needed?
    @Overwrite
    //? if >= 1.21.11 {
    private Identifier getIdentifier(String type, Identifier key) {
    //?} else
    /*private ResourceLocation getResourceLocation(String type, ResourceLocation key) {*/
        var textures = this.polydium$textures.get(type);
        if (textures == null) {
            textures = new HashMap<>();
            this.polydium$textures.put(type, textures);
        }
        var texture = textures.get(key);
        if (texture == null) {
            texture = key.withPath(keyPath -> "textures/entity/" + this.path + "/" + type + "/" + keyPath + ".png");
            textures.put(key, texture);
        }
        return texture;
    }
}
