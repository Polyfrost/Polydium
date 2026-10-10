package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
//? if >= 26.1 {
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
//?} else {
/*import net.minecraft.client.renderer.PerspectiveProjectionMatrixBuffer;
*///?}
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.ByteBuffer;

//? if >= 26.1 {
@Mixin(ProjectionMatrixBuffer.class)
//?} else {
/*@Mixin(PerspectiveProjectionMatrixBuffer.class)
*///?}
public class MixinProjectionUploadSkip {
    @Unique
    private Matrix4f polydium$uploaded;

    @WrapOperation(
        //? if >= 26.1 {
        method = "writeBuffer",
        //?} else {
        /*method = "getBuffer",
        *///?}
        at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/CommandEncoder;writeToBuffer(Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;Ljava/nio/ByteBuffer;)V")
    )
    private void polydium$skipUnchanged(CommandEncoder encoder, GpuBufferSlice destination, ByteBuffer data, Operation<Void> original, @Local(argsOnly = true) Matrix4f projectionMatrix) {
        Matrix4f uploaded = this.polydium$uploaded;
        if (uploaded == null) {
            this.polydium$uploaded = new Matrix4f(projectionMatrix);
        } else if (polydium$sameBits(uploaded, projectionMatrix)) {
            return;
        } else {
            uploaded.set(projectionMatrix);
        }
        original.call(encoder, destination, data);
    }

    @Unique
    private static boolean polydium$sameBits(Matrix4f a, Matrix4f b) {
        return polydium$same(a.m00(), b.m00()) && polydium$same(a.m01(), b.m01()) && polydium$same(a.m02(), b.m02()) && polydium$same(a.m03(), b.m03())
            && polydium$same(a.m10(), b.m10()) && polydium$same(a.m11(), b.m11()) && polydium$same(a.m12(), b.m12()) && polydium$same(a.m13(), b.m13())
            && polydium$same(a.m20(), b.m20()) && polydium$same(a.m21(), b.m21()) && polydium$same(a.m22(), b.m22()) && polydium$same(a.m23(), b.m23())
            && polydium$same(a.m30(), b.m30()) && polydium$same(a.m31(), b.m31()) && polydium$same(a.m32(), b.m32()) && polydium$same(a.m33(), b.m33());
    }

    @Unique
    private static boolean polydium$same(float a, float b) {
        return Float.floatToRawIntBits(a) == Float.floatToRawIntBits(b);
    }
}
//?}
