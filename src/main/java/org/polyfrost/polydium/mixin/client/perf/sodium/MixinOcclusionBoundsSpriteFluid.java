package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.8 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.polyfrost.polydium.client.perf.SectionBounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer", remap = false)
public abstract class MixinOcclusionBoundsSpriteFluid {
    @ModifyArg(method = "writeQuad", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuilder;addSprite"))
    private TextureAtlasSprite polydium$trackAnimatedCutout(TextureAtlasSprite sprite, @Local(argsOnly = true) Material material) {
        SectionBounds.spriteAhead(sprite, material);

        return sprite;
    }

    @ModifyExpressionValue(method = "writeQuad", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/translucent_sorting/TranslucentGeometryCollector;appendQuad"))
    private boolean polydium$forgetDiscarded(boolean discarded) {
        if (discarded)
            SectionBounds.quadDiscarded();

        return discarded;
    }
}
//?}
