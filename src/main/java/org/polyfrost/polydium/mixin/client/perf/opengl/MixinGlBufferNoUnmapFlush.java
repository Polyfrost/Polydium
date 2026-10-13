package org.polyfrost.polydium.mixin.client.perf.opengl;

//? if >= 26.2 && < 26.3 {
/*import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.renderpearl.backend.opengl.DirectStateAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlBuffer$Direct$1")
public abstract class MixinGlBufferNoUnmapFlush {

    @WrapWithCondition(method = "run", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/DirectStateAccess;flushMappedBufferRange(IJJI)V"))
    private boolean polydium$skipFlush(DirectStateAccess dsa, int handle, long offset, long length, int usage) {
        return false;
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
