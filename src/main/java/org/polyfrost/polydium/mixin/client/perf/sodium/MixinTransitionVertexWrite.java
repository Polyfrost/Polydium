package org.polyfrost.polydium.mixin.client.perf.sodium;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.api.vertex.format.common.EntityVertex;
import org.joml.Matrix4f;
import org.polyfrost.polydium.client.perf.DirectVertexBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "dev.tr7zw.transition.mc.VertexConsumerUtil", remap = false)
public abstract class MixinTransitionVertexWrite {
    @WrapMethod(
            method = /*? if >= 26.1 {*/"addVertex(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lorg/joml/Matrix4f;FFFFFIIIFFFI)V"/*?} else {*//*"addVertex(Lnet/minecraft/class_4588;Lorg/joml/Matrix4f;FFFFFIIIFFFI)V"*//*?}*/,
            require = 0,
            expect = 0
    )
    private static void polydium$writeVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float u, float v,
                                             int overlay, int blockLight, int skyLight, float normalX, float normalY, float normalZ, int alpha,
                                             Operation<Void> original) {
        long pointer = DirectVertexBuffers.reserve(consumer, EntityVertex.FORMAT, EntityVertex.STRIDE, 1);
        if (pointer == 0L) {
            original.call(consumer, matrix, x, y, z, u, v, overlay, blockLight, skyLight, normalX, normalY, normalZ, alpha);
            return;
        }
        EntityVertex.write(pointer,
                matrix.m00() * x + matrix.m10() * y + matrix.m20() * z + matrix.m30(),
                matrix.m01() * x + matrix.m11() * y + matrix.m21() * z + matrix.m31(),
                matrix.m02() * x + matrix.m12() * y + matrix.m22() * z + matrix.m32(),
                (alpha & 0xFF) << 24 | 0xFFFFFF, u, v, overlay,
                blockLight & 0xFFFF | (skyLight & 0xFFFF) << 16,
                NormI8.pack(normalX, normalY, normalZ));
    }
}
