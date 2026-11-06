package org.polyfrost.polydium.mixin.client.perf;

//? if < 26.1 {
/*import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BlockMarker;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.TerrainParticle;
import org.polyfrost.polydium.client.perf.OpaqueParticles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
//? if >= 1.21.10 {
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//?} else {
/^import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
^///?}

@Mixin(value = {TerrainParticle.class, BreakingItemParticle.class, BlockMarker.class}, priority = 1100)
//? if >= 1.21.10 {
public abstract class MixinOpaqueSpriteParticle extends SingleQuadParticle {
    protected MixinOpaqueSpriteParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);
    }

    @ModifyReturnValue(method = "getLayer", at = @At("RETURN"))
    private SingleQuadParticle.Layer polydium$opaqueLayer(SingleQuadParticle.Layer layer) {
        return OpaqueParticles.layer(layer, this.sprite, this.alpha);
    }
}
//?} else {
/^public abstract class MixinOpaqueSpriteParticle extends TextureSheetParticle {
    protected MixinOpaqueSpriteParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @ModifyReturnValue(method = "getRenderType", at = @At("RETURN"))
    private ParticleRenderType polydium$opaqueRenderType(ParticleRenderType type) {
        return OpaqueParticles.renderType(type, this.sprite, this.alpha, this.getClass());
    }
}
^///?}
*///?}
