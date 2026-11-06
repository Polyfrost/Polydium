package org.polyfrost.polydium.mixin.client.perf.opengl;

//? if >= 1.21.11 && < 26.2 {
/*import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.lwjgl.opengl.GL15;
import org.polyfrost.polydium.client.perf.ImmediateDrawRing;
import org.polyfrost.polydium.mixin.client.perf.GlBufferAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.mojang.blaze3d.opengl.VertexArrayCache$Emulated")
public abstract class MixinImmediateDrawRingEmulated {
    @Shadow
    private static void setupCombinedAttributes(VertexFormat format, boolean enable) {
        throw new AssertionError();
    }

    @Inject(method = "bindVertexArray", at = @At("RETURN"))
    private void polydium$bindRingOffset(VertexFormat format, GlBuffer buffer, CallbackInfo ci) {
        if (ImmediateDrawRing.holds(buffer)) {
            GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, ((GlBufferAccessor) buffer).polydium$handle());
            setupCombinedAttributes(format, false);
        }
    }

    @ModifyArg(method = "setupCombinedAttributes", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_vertexAttribPointer(IIIZIJ)V"), index = 5)
    private static long polydium$offsetPointer(long pointer) {
        return pointer + ImmediateDrawRing.vertexOffset();
    }

    @ModifyArg(method = "setupCombinedAttributes", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_vertexAttribIPointer(IIIIJ)V"), index = 4)
    private static long polydium$offsetIntPointer(long pointer) {
        return pointer + ImmediateDrawRing.vertexOffset();
    }
}
*///?}
