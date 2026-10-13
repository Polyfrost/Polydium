package org.polyfrost.polydium.mixin.client.perf.opengl;

//? if >= 26.2 && < 26.3 {
/*import com.mojang.renderpearl.backend.opengl.GlStateManager;
import org.lwjgl.opengl.GL33C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GlStateManager.class)
public abstract class MixinGlBlendEnableIndexed {

    @Redirect(method = "_enableBlend", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager$BooleanState;enable()V"))
    private static void polydium$enableIndexed(@Coerce Object mode, int index) {
        GlBooleanStateAccessor state = (GlBooleanStateAccessor) mode;
        if (!state.polydium$enabled()) {
            state.polydium$setEnabled(true);
            GL33C.glEnablei(3042, index);
        }
    }

    @Redirect(method = "_disableBlend", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager$BooleanState;disable()V"))
    private static void polydium$disableIndexed(@Coerce Object mode, int index) {
        GlBooleanStateAccessor state = (GlBooleanStateAccessor) mode;
        if (state.polydium$enabled()) {
            state.polydium$setEnabled(false);
            GL33C.glDisablei(3042, index);
        }
    }
}
*///?}
