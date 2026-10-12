package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.10 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.common.ParticleVertex;
import net.minecraft.client.particle.SingleQuadParticle;
//? if >= 26.1 {
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
//?} else {
/*import net.minecraft.client.renderer.state.QuadParticleRenderState;
*///?}
//? if < 26.2 {
/*import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.injection.Coerce;
*///?}
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.polyfrost.polydium.client.perf.ParticleQuadSupport;
import org.polyfrost.polydium.mixin.client.perf.ParticleBufferBuilderAccessor;
import org.polyfrost.polydium.mixin.client.perf.QuadParticleStorageAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = QuadParticleRenderState.class, priority = 1100)
public abstract class MixinQuadParticleBatchWrite {
    @Unique
    private static final int POLYDIUM_QUAD_BYTES = 4 * ParticleVertex.STRIDE;
    @Unique
    private static final int POLYDIUM_MAX_BATCH = 1 << 22;

    @Unique
    private static long polydium$scratch;
    @Unique
    private static int polydium$scratchBytes;

    @Shadow
    @Final
    private Map<SingleQuadParticle.Layer, ?> particles;

    @Inject(method = "buildLayer", at = @At("HEAD"), cancellable = true, require = 0, expect = 0)
    private void polydium$buildLayerBatched(SingleQuadParticle.Layer layer, VertexConsumer builder, CallbackInfo ci) {
        if (builder.getClass() != BufferBuilder.class
                || ((Object) this).getClass() != QuadParticleRenderState.class) {
            return;
        }
        Object storage = this.particles.get(layer);
        if (storage == null
                || ((ParticleBufferBuilderAccessor) builder).polydium$format() != ParticleVertex.FORMAT
                || !ParticleQuadSupport.canBatch(QuadParticleRenderState.class, storage.getClass())) {
            return;
        }
        VertexBufferWriter writer = VertexBufferWriter.tryOf(builder);
        QuadParticleStorageAccessor stored = (QuadParticleStorageAccessor) storage;
        int count = stored.polydium$particleCount();
        if (writer == null || count <= 0 || count > POLYDIUM_MAX_BATCH) {
            return;
        }

        ci.cancel();
        float[] floats = stored.polydium$floatValues();
        int[] ints = stored.polydium$intValues();
        ParticleBufferBuilderAccessor target = (ParticleBufferBuilderAccessor) builder;
        int bytes = count * POLYDIUM_QUAD_BYTES;
        boolean direct = target.polydium$vertexSize() == ParticleVertex.STRIDE && ParticleQuadSupport.canWriteDirect(BufferBuilder.class);
        long buffer = direct ? target.polydium$buffer().reserve(bytes) : polydium$scratch(bytes);
        long ptr = buffer;

        for (int particle = 0; particle < count; particle++) {
            int f = particle * 12;
            float x = floats[f];
            float y = floats[f + 1];
            float z = floats[f + 2];
            float[] r = ParticleQuadSupport.rotatedCorners(floats[f + 3], floats[f + 4], floats[f + 5], floats[f + 6]);
            float size = floats[f + 7];
            float u0 = floats[f + 8];
            float u1 = floats[f + 9];
            float v0 = floats[f + 10];
            float v1 = floats[f + 11];
            int color = ColorARGB.toABGR(ints[particle * 2]);
            int light = ints[particle * 2 + 1];

            ParticleVertex.put(ptr, r[0] * size + x, r[1] * size + y, r[2] * size + z, u1, v1, color, light);
            ptr += ParticleVertex.STRIDE;
            ParticleVertex.put(ptr, r[3] * size + x, r[4] * size + y, r[5] * size + z, u1, v0, color, light);
            ptr += ParticleVertex.STRIDE;
            ParticleVertex.put(ptr, r[6] * size + x, r[7] * size + y, r[8] * size + z, u0, v0, color, light);
            ptr += ParticleVertex.STRIDE;
            ParticleVertex.put(ptr, r[9] * size + x, r[10] * size + y, r[11] * size + z, u0, v1, color, light);
            ptr += ParticleVertex.STRIDE;
        }

        if (direct) {
            target.polydium$setVertices(target.polydium$vertices() + count * 4);
            target.polydium$setVertexPointer(buffer + bytes - ParticleVertex.STRIDE);
            target.polydium$setElementsToFill(0);
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            writer.push(stack, buffer, count * 4, ParticleVertex.FORMAT);
        }
    }

    //? if < 26.2 {
    /*@WrapOperation(
            method = "prepare",
            at = @At(value = "INVOKE", target = /^? if >= 26.1 {^/"Lnet/minecraft/client/renderer/state/level/QuadParticleRenderState$Storage;forEachParticle(Lnet/minecraft/client/renderer/state/level/QuadParticleRenderState$ParticleConsumer;)V"/^?} else {^//^"Lnet/minecraft/client/renderer/state/QuadParticleRenderState$Storage;forEachParticle(Lnet/minecraft/client/renderer/state/QuadParticleRenderState$ParticleConsumer;)V"^//^?}^/),
            require = 0,
            expect = 0
    )
    private void polydium$prepareLayerBatched(@Coerce Object storage, QuadParticleRenderState.ParticleConsumer consumer, Operation<Void> original,
                                              @Local Map.Entry<SingleQuadParticle.Layer, ?> entry, @Local BufferBuilder builder) {
        CallbackInfo ci = new CallbackInfo("prepare", true);
        this.polydium$buildLayerBatched(entry.getKey(), builder, ci);
        if (!ci.isCancelled()) {
            original.call(storage, consumer);
        }
    }

    *///?}
    @WrapOperation(
            method = "sodium$emitVertices",
            at = @At(value = "INVOKE", target = "Lorg/joml/Vector3f;rotate(Lorg/joml/Quaternionfc;)Lorg/joml/Vector3f;"),
            remap = false,
            require = 0,
            expect = 0
    )
    private Vector3f polydium$reuseRotatedCorner(Vector3f corner, Quaternionfc rotation, Operation<Vector3f> original) {
        if (Float.floatToRawIntBits(corner.z) != 0
                || Math.abs(corner.x) != 1.0F
                || Math.abs(corner.y) != 1.0F) {
            return original.call(corner, rotation);
        }
        float[] r = ParticleQuadSupport.rotatedCorners(rotation.x(), rotation.y(), rotation.z(), rotation.w());
        int i = corner.x > 0.0F ? (corner.y < 0.0F ? 0 : 3) : (corner.y > 0.0F ? 6 : 9);
        return corner.set(r[i], r[i + 1], r[i + 2]);
    }

    @Unique
    private static long polydium$scratch(int bytes) {
        if (bytes > polydium$scratchBytes) {
            MemoryUtil.nmemFree(polydium$scratch);
            int size = Math.max(Integer.highestOneBit(bytes - 1) << 1, 1 << 16);
            polydium$scratch = MemoryUtil.nmemAllocChecked(size);
            polydium$scratchBytes = size;
        }
        return polydium$scratch;
    }
}
//?}
