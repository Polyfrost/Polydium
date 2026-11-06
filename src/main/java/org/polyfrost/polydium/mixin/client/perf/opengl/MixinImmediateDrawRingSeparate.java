package org.polyfrost.polydium.mixin.client.perf.opengl;

//? if >= 1.21.11 && < 26.2 {
/*import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.lwjgl.opengl.ARBVertexAttribBinding;
import org.polyfrost.polydium.client.perf.ImmediateDrawRing;
import org.polyfrost.polydium.mixin.client.perf.GlBufferAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.mojang.blaze3d.opengl.VertexArrayCache$Separate")
public abstract class MixinImmediateDrawRingSeparate {
    @Inject(method = "bindVertexArray", at = @At("RETURN"))
    private void polydium$bindRingOffset(VertexFormat format, GlBuffer buffer, CallbackInfo ci) {
        if (ImmediateDrawRing.holds(buffer)) {
            ARBVertexAttribBinding.glBindVertexBuffer(0, ((GlBufferAccessor) buffer).polydium$handle(), ImmediateDrawRing.vertexOffset(), format.getVertexSize());
        }
    }
}
*///?}
