package org.polyfrost.polydium.mixin.client.perf.opengl;

//? if >= 1.21.8 && < 26.3 {
/*import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.renderpearl.backend.opengl.GlConst;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GlConst.class)
public abstract class MixinGlBufferStorageCoherent {

    @ModifyReturnValue(method = "bufferUsageToGlFlag", at = @At("RETURN"))
    private static int polydium$coherentWhenPersistent(int flags) {
        return (flags & 64) != 0 ? flags | 128 : flags;
    }
}
*///?}
