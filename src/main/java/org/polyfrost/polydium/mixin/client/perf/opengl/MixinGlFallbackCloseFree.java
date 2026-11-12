package org.polyfrost.polydium.mixin.client.perf.opengl;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
//? if >= 26.3 {
import com.mojang.renderpearl.backend.util.TransientBlockAllocator;
//?} else {
/*import com.mojang.blaze3d.util.TransientBlockAllocator;
*///?}
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = /*? if >= 26.3 {*/"com.mojang.renderpearl.backend.opengl.GlTransientMemory$Fallback"/*?} else {*//*"com.mojang.blaze3d.opengl.GlTransientMemory$Fallback"*//*?}*/)
public abstract class MixinGlFallbackCloseFree {
    @Shadow
    @Final
    private TransientBlockAllocator<?> blockAllocator;

    @WrapMethod(method = "close")
    private void polydium$freeAllBlocks(Operation<Void> original) {
        original.call();
        blockAllocator.close();
    }
}
//?}
