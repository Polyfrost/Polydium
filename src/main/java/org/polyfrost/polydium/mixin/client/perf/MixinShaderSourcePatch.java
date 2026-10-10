package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import org.polyfrost.polydium.client.perf.IrisShaders;
import org.polyfrost.polydium.client.perf.SkyFarPlane;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShaderManager.class)
public abstract class MixinShaderSourcePatch {
    @Unique
    private static final String polydium$RGSS_SHADER = "shaders/blocks/block_layer_opaque.fsh";
    @Unique
    private static final String polydium$RGSS_BLEND = "    float blendFactor = smoothstep(transitionStart, transitionEnd, maxTexelSize);\n";
    @Unique
    private static final String polydium$RGSS_SKIP = "    if (blendFactor == 0.0) {\n        return sampleNearest(source, uv, pixelSize, du, dv, texelScreenSize);\n    }\n";

    //? if >= 26.3 {
    @ModifyExpressionValue(method = "loadShader", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/Resource;readAllAsString()Ljava/lang/String;"))
    //?} else {
    /*@ModifyExpressionValue(method = "loadShader", at = @At(value = "INVOKE", target = "Lorg/apache/commons/io/IOUtils;toString(Ljava/io/Reader;)Ljava/lang/String;"))
    *///?}
    private static String polydium$patchSources(String contents, @Local(argsOnly = true) Identifier location) {
        contents = SkyFarPlane.patch(location, contents);
        return polydium$rgssSkip(location, contents);
    }

    @Unique
    private static String polydium$rgssSkip(Identifier location, String source) {
        if (!location.getNamespace().equals("sodium") || !location.getPath().equals(polydium$RGSS_SHADER)
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
