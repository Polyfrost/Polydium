package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import org.joml.Matrix4f;
//? if < 26.2 {
/*import org.joml.Vector3f;
import org.joml.Vector4f;
import org.polyfrost.polydium.client.perf.TransformWrites;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

// above default so the wrap can still land inside VulkanMod's @Overwrite of draw
@Mixin(value = RenderType.class, priority = 1100)
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

    //? if < 26.2 {
    /*@WrapOperation(method = "draw", at = @At(value = "NEW", target = "(FFFF)Lorg/joml/Vector4f;"), require = 0)
    private Vector4f polydium$sharedWhite(float r, float g, float b, float a, Operation<Vector4f> original) {
        return r == 1.0F && g == 1.0F && b == 1.0F && a == 1.0F ? TransformWrites.WHITE : original.call(r, g, b, a);
    }

    @WrapOperation(method = "draw", at = @At(value = "NEW", target = "()Lorg/joml/Vector3f;"), require = 0)
    private Vector3f polydium$sharedZero(Operation<Vector3f> original) {
        return TransformWrites.ZERO;
    }
    *///?}
}
//?}
