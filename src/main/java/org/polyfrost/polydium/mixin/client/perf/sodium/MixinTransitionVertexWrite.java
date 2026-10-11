package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 {
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.api.vertex.format.common.EntityVertex;
import org.joml.Matrix4f;
import org.polyfrost.polydium.client.perf.DirectVertexBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.tr7zw.transition.mc.VertexConsumerUtil", remap = false)
public abstract class MixinTransitionVertexWrite {
    @Inject(
            method = /*? if >= 26.1 {*/"addVertex(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lorg/joml/Matrix4f;FFFFFIIIFFFI)V"/*?} else {*//*"addVertex(Lnet/minecraft/class_4588;Lorg/joml/Matrix4f;FFFFFIIIFFFI)V"*//*?}*/,
            at = @At("HEAD"),
            cancellable = true,
            require = 0,
            expect = 0
    )
    private static void polydium$writeVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float u, float v,
                                             int overlay, int blockLight, int skyLight, float normalX, float normalY, float normalZ, int alpha,
                                             CallbackInfo ci) {
        long pointer = DirectVertexBuffers.reserve(consumer, EntityVertex.FORMAT, EntityVertex.STRIDE, 1);
        if (pointer == 0L) {
            return;
        }
        EntityVertex.write(pointer,
                matrix.m00() * x + matrix.m10() * y + matrix.m20() * z + matrix.m30(),
                matrix.m01() * x + matrix.m11() * y + matrix.m21() * z + matrix.m31(),
                matrix.m02() * x + matrix.m12() * y + matrix.m22() * z + matrix.m32(),
                (alpha & 0xFF) << 24 | 0xFFFFFF, u, v, overlay,
                blockLight & 0xFFFF | (skyLight & 0xFFFF) << 16,
                NormI8.pack(normalX, normalY, normalZ));
        ci.cancel();
    }
}
//?}
