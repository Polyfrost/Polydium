package org.polyfrost.polydium.mixin.client.perf.opengl;

//? if >= 26.2 && < 26.3 {
/*import com.mojang.renderpearl.backend.opengl.GlBuffer;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GlBuffer.Direct.class)
public abstract class MixinGlBufferMapFlags {

    @Unique
    private static Boolean polydium$glOnDx12;

    @ModifyArg(method = "map", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/DirectStateAccess;mapBufferRange(IJJII)Ljava/nio/ByteBuffer;"), index = 3)
    private int polydium$coherentMapFlags(int flags) {
        if ((flags & 2) != 0) {
            flags &= ~16;
            if (polydium$glOnDx12 == null) {
                polydium$glOnDx12 = Util.getPlatform() == Util.OS.WINDOWS && Util.isAarch64() || GlStateManager._getString(7937).startsWith("D3D12");
            }
            if (polydium$glOnDx12) {
                flags &= ~32;
            }
        }
        return (flags & 64) != 0 ? flags | 128 : flags;
    }
}
*///?} else if >= 1.21.8 && < 26.2 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "com.mojang.blaze3d.opengl.BufferStorage$Immutable")
public abstract class MixinGlBufferMapFlags {

    @ModifyExpressionValue(method = "tryMapBufferPersistent", at = @At(value = "CONSTANT", args = "intValue=18"))
    private int polydium$noFlushExplicit(int flags) {
        return flags & ~16;
    }

    @ModifyExpressionValue(method = "tryMapBufferPersistent", at = @At(value = "CONSTANT", args = "intValue=64"))
    private int polydium$coherent(int flags) {
        return flags | 128;
    }
}
*///?}
