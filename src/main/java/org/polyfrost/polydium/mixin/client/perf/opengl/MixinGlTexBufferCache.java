package org.polyfrost.polydium.mixin.client.perf.opengl;

//? if >= 1.21.8 {
import com.google.common.collect.MapMaker;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.backend.opengl.Uniform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.Map;

@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlCommandEncoder")
public abstract class MixinGlTexBufferCache {
    @Unique
    private static final Map<Uniform.Utb, GpuBuffer> polydium$attached = new MapMaker().weakKeys().makeMap();

    @Unique
    private Uniform.Utb polydium$utb;
    @Unique
    private GpuBuffer polydium$buffer;

    @Redirect(method = {"trySetup", "setupDraw"}, at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/Uniform$Utb;texture()I"))
    private int polydium$captureUtb(Uniform.Utb utb) {
        polydium$utb = utb;
        return utb.texture();
    }

    @ModifyExpressionValue(method = {"trySetup", "setupDraw"}, slice = @Slice(from = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/Uniform$Utb;texture()I")), at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;buffer()Lcom/mojang/renderpearl/api/buffers/GpuBuffer;", ordinal = 0))
    private GpuBuffer polydium$captureBuffer(GpuBuffer buffer) {
        polydium$buffer = buffer;
        return buffer;
    }

    @WrapWithCondition(method = {"trySetup", "setupDraw"}, at = {
            @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL33C;glTexBuffer(III)V"),
            @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL31;glTexBuffer(III)V")
    })
    private boolean polydium$attachIfChanged(int target, int format, int handle) {
        Uniform.Utb utb = polydium$utb;
        GpuBuffer buffer = polydium$buffer;
        polydium$utb = null;
        polydium$buffer = null;
        if (utb == null || buffer == null || buffer.isClosed()) {
            return true;
        }

        if (polydium$attached.get(utb) == buffer) {
            return false;
        }

        polydium$attached.put(utb, buffer);
        return true;
    }
}
//?}
