package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderType.class)
public class MixinDefaultTextureMatrix {
    @Unique
    private static final Matrix4f POLYDIUM_IDENTITY = new Matrix4f();

    @WrapOperation(
        //? if >= 26.2 {
        method = "writeDynamicTransforms",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/TextureTransform;createMatrix()Lorg/joml/Matrix4f;")
        //?} else {
        /*method = "draw",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/TextureTransform;getMatrix()Lorg/joml/Matrix4f;")
        *///?}
    )
    private Matrix4f polydium$sharedIdentity(TextureTransform transform, Operation<Matrix4f> original) {
        if (transform == TextureTransform.DEFAULT_TEXTURING) {
            return POLYDIUM_IDENTITY;
        }
        return original.call(transform);
    }
}
//?}
