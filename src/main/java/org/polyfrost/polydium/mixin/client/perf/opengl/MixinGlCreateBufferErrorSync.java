package org.polyfrost.polydium.mixin.client.perf.opengl;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.ByteBuffer;

@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlDevice")
public abstract class MixinGlCreateBufferErrorSync {
    @Unique
    private static final long polydium$CHECKED_SIZE = 1L << 20;

    @WrapOperation(method = "createBuffer(Ljava/util/function/Supplier;IJ)Lcom/mojang/renderpearl/api/buffers/GpuBuffer;",
            at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager;clearGlErrors()V"))
    private void polydium$skipClear(Operation<Void> original, @Local(argsOnly = true) long size) {
        if (size >= polydium$CHECKED_SIZE)
            original.call();
    }

    @WrapOperation(method = "createBuffer(Ljava/util/function/Supplier;IJ)Lcom/mojang/renderpearl/api/buffers/GpuBuffer;",
            at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_getError()I"))
    private int polydium$skipCheck(Operation<Integer> original, @Local(argsOnly = true) long size) {
        return size >= polydium$CHECKED_SIZE ? original.call() : 0;
    }

    @WrapOperation(method = "createBuffer(Ljava/util/function/Supplier;ILjava/nio/ByteBuffer;)Lcom/mojang/renderpearl/api/buffers/GpuBuffer;",
            at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager;clearGlErrors()V"))
    private void polydium$skipClearData(Operation<Void> original, @Local(argsOnly = true) ByteBuffer data) {
        if (data.remaining() >= polydium$CHECKED_SIZE)
            original.call();
    }

    @WrapOperation(method = "createBuffer(Ljava/util/function/Supplier;ILjava/nio/ByteBuffer;)Lcom/mojang/renderpearl/api/buffers/GpuBuffer;",
            at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_getError()I"))
    private int polydium$skipCheckData(Operation<Integer> original, @Local(argsOnly = true) ByteBuffer data) {
        return data.remaining() >= polydium$CHECKED_SIZE ? original.call() : 0;
    }
}
//?}
