package org.polyfrost.polydium.client.perf;

//? if < 26.1 {
/*//? if >= 1.21.10 {
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
//?} else if >= 1.21.4 {
/^import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.RenderType;
^///?} else {
/^import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;
^///?}
//? if < 1.21.10 {
/^import net.minecraft.client.particle.BlockMarker;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.TerrainParticle;
^///?}
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class OpaqueParticles {
    //? if >= 1.21.10 {
    public static final SingleQuadParticle.Layer TERRAIN = new SingleQuadParticle.Layer(false, TextureAtlas.LOCATION_BLOCKS, RenderPipelines.OPAQUE_PARTICLE);
    //? if >= 1.21.11
    public static final SingleQuadParticle.Layer ITEMS = new SingleQuadParticle.Layer(false, TextureAtlas.LOCATION_ITEMS, RenderPipelines.OPAQUE_PARTICLE);

    public static SingleQuadParticle.Layer layer(SingleQuadParticle.Layer layer, TextureAtlasSprite sprite, float alpha) {
        if (alpha < 1.0F) {
            return layer;
        }
        if (layer == SingleQuadParticle.Layer.TERRAIN) {
            return SpriteTransparency.hasTranslucent(sprite) ? layer : TERRAIN;
        }
        //? if >= 1.21.11 {
        if (layer == SingleQuadParticle.Layer.ITEMS) {
            return SpriteTransparency.hasTranslucent(sprite) ? layer : ITEMS;
        }
        //?}
        return layer;
    }
    //?} else if >= 1.21.4 {
    /^public static final ParticleRenderType TERRAIN_SHEET = new ParticleRenderType("POLYDIUM_TERRAIN_SHEET_OPAQUE", RenderType.opaqueParticle(TextureAtlas.LOCATION_BLOCKS));
    ^///?} else {
    /^public static final ParticleRenderType TERRAIN_SHEET = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public String toString() {
            return "POLYDIUM_TERRAIN_SHEET_OPAQUE";
        }
    };
    ^///?}

    //? if < 1.21.10 {
    /^// The engine groups a particle by this type once, when it is added, so a subclass that fades later could never leave the opaque group. Only exact vanilla classes, which never fade, qualify.
    public static ParticleRenderType renderType(ParticleRenderType type, TextureAtlasSprite sprite, float alpha, Class<?> particle) {
        boolean neverFades = particle == TerrainParticle.class || particle == BreakingItemParticle.class || particle == BlockMarker.class;
        return type == ParticleRenderType.TERRAIN_SHEET && neverFades && alpha >= 1.0F && sprite != null && !SpriteTransparency.hasTranslucent(sprite) ? TERRAIN_SHEET : type;
    }
    ^///?}

    private OpaqueParticles() {
    }
}
*///?}
