package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 && < 26.2 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.DynamicUniforms;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.polyfrost.polydium.client.perf.TransformWrites;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DynamicUniforms.class)
public abstract class MixinTransformSharedDefaults {
    @WrapOperation(method = "writeTransform", at = @At(value = "NEW", target = "(Lorg/joml/Vector4fc;)Lorg/joml/Vector4f;"))
    private Vector4f polydium$sharedWhite(Vector4fc color, Operation<Vector4f> original) {
        return color.x() == 1.0F && color.y() == 1.0F && color.z() == 1.0F && color.w() == 1.0F ? TransformWrites.WHITE : original.call(color);
    }

    @WrapOperation(method = "writeTransform", at = @At(value = "NEW", target = "(Lorg/joml/Vector3fc;)Lorg/joml/Vector3f;"))
    private Vector3f polydium$sharedZero(Vector3fc offset, Operation<Vector3f> original) {
        return offset.x() == 0.0F && offset.y() == 0.0F && offset.z() == 0.0F ? TransformWrites.ZERO : original.call(offset);
    }

    @WrapOperation(method = "writeTransform", at = @At(value = "NEW", target = "(Lorg/joml/Matrix4fc;)Lorg/joml/Matrix4f;", ordinal = 1))
    private Matrix4f polydium$sharedIdentity(Matrix4fc textureMatrix, Operation<Matrix4f> original) {
        return (textureMatrix.properties() & Matrix4fc.PROPERTY_IDENTITY) != 0 ? TransformWrites.IDENTITY : original.call(textureMatrix);
    }
}
*///?}
