package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if >= 26.1 {
import net.minecraft.client.renderer.state.gui.GlyphRenderState;
//?} else {
/*import net.minecraft.client.gui.render.state.GlyphRenderState;
*///?}
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GlyphRenderState.class)
public class MixinGuiGlyphMatrixReuse {
    @Unique
    private static final Matrix4f POLYDIUM_SCRATCH = new Matrix4f();

    @WrapOperation(method = "buildVertices", at = @At(value = "NEW", target = "()Lorg/joml/Matrix4f;"))
    private Matrix4f polydium$reuseMatrix(Operation<Matrix4f> original) {
        return POLYDIUM_SCRATCH.identity();
    }
}
//?}
