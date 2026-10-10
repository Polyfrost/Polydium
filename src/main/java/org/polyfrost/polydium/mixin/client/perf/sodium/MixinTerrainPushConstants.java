package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer", remap = false)
public abstract class MixinTerrainPushConstants {
    @Unique
    private static ByteBuffer polydium$constants;

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/MemoryStack;malloc(I)Ljava/nio/ByteBuffer;"))
    private ByteBuffer polydium$reuseConstants(MemoryStack stack, int size, Operation<ByteBuffer> original) {
        ByteBuffer constants = polydium$constants;
        if (constants == null || constants.capacity() < size) {
            polydium$constants = constants = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
        }
        return constants.clear().limit(size);
    }
}
//?}
