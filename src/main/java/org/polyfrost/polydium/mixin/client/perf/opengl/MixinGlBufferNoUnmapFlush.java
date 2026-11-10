package org.polyfrost.polydium.mixin.client.perf.opengl;

//? if >= 26.2 && < 26.3 {
/*import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = {"com.mojang.renderpearl.backend.opengl.DirectStateAccess$Core", "com.mojang.renderpearl.backend.opengl.DirectStateAccess$Emulated"})
public abstract class MixinGlBufferNoUnmapFlush {

    @Inject(method = "flushMappedBufferRange", at = @At("HEAD"), cancellable = true)
    private void polydium$skipFlush(int handle, long offset, long length, int usage, CallbackInfo ci) {
        ci.cancel();
    }
}
*///?} else if >= 1.21.11 && < 26.2 {
/*import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "com.mojang.blaze3d.opengl.BufferStorage$Immutable")
public abstract class MixinGlBufferNoUnmapFlush {

    @ModifyVariable(method = "mapBuffer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int polydium$skipFlush(int flags) {
        return flags & ~2;
    }
}
*///?} else if >= 1.21.8 && < 1.21.11 {
/*import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "com.mojang.blaze3d.opengl.BufferStorage$Immutable")
public abstract class MixinGlBufferNoUnmapFlush {

    @ModifyVariable(method = "mapBuffer", at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private int polydium$skipFlush(int flags) {
        return flags & ~2;
    }
}
*///?}
