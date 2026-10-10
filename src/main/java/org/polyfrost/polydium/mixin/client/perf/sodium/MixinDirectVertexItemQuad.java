package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.common.EntityVertex;
import org.lwjgl.system.MemoryStack;
import org.polyfrost.polydium.client.perf.DirectVertexBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.immediate.model.BakedModelEncoder", remap = false)
public abstract class MixinDirectVertexItemQuad {
    @WrapOperation(method = /*? if >= 26.1 {*/"writeQuadVertices"/*?} else {*//*{"writeQuadVertices(Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/caffeinemc/mods/sodium/client/model/quad/ModelQuadView;IIIZ)V", "writeQuadVertices(Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/caffeinemc/mods/sodium/client/model/quad/ModelQuadView;FFFF[F[II)V"}*//*?}*/, at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/MemoryStack;nmalloc(I)J"))
    private static long polydium$writeIntoTarget(MemoryStack stack, int size, Operation<Long> original,
                                                 @Local(argsOnly = true) VertexBufferWriter writer,
                                                 @Share("polydium$direct") LocalBooleanRef direct) {
        long pointer = DirectVertexBuffers.reserve(writer, EntityVertex.FORMAT, EntityVertex.STRIDE, 4);
        if (pointer != 0L) {
            direct.set(true);
            return pointer;
        }
        return original.call(stack, size);
    }

    @WrapOperation(
            method = /*? if >= 26.1 {*/"writeQuadVertices"/*?} else {*//*{"writeQuadVertices(Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/caffeinemc/mods/sodium/client/model/quad/ModelQuadView;IIIZ)V", "writeQuadVertices(Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/caffeinemc/mods/sodium/client/model/quad/ModelQuadView;FFFF[F[II)V"}*//*?}*/,
            at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;push(Lorg/lwjgl/system/MemoryStack;JILcom/mojang/renderpearl/api/vertex/VertexFormat;)V")
    )
    private static void polydium$skipCopy(VertexBufferWriter writer, MemoryStack stack, long source, int count, VertexFormat format, Operation<Void> original,
                                          @Share("polydium$direct") LocalBooleanRef direct) {
        if (!direct.get()) {
            original.call(writer, stack, source, count, format);
        }
    }
}
//?}
