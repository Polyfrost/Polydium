package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.resources.Identifier;
import org.polyfrost.polydium.client.perf.IrisShaders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.gl.shader.ShaderLoader", remap = false)
public abstract class MixinTerrainRgssSkip {
    @Unique
    private static final String polydium$RGSS_BLEND = "    float blendFactor = smoothstep(transitionStart, transitionEnd, maxTexelSize);\n";
    @Unique
    private static final String polydium$RGSS_SKIP = "    if (blendFactor == 0.0) {\n        return sampleNearest(source, uv, pixelSize, du, dv, texelScreenSize);\n    }\n";

    @ModifyReturnValue(method = "getShaderSource", at = @At("RETURN"))
    private static String polydium$rgssSkip(String source, Identifier name) {
        if (!name.getNamespace().equals("sodium") || !name.getPath().equals("blocks/block_layer_opaque.fsh")
                || IrisShaders.packInUse()) {
            return source;
        }
        String normalized = source.replace("\r\n", "\n");
        if (!normalized.contains(polydium$RGSS_BLEND)) {
            return source;
        }
        return normalized.replace(polydium$RGSS_BLEND, polydium$RGSS_BLEND + polydium$RGSS_SKIP);
    }
}
//?}
