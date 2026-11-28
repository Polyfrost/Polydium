package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.2 {
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.caffeinemc.mods.sodium.client.gpu.device.batch.MultiDrawBatch;
import net.caffeinemc.mods.sodium.client.gpu.device.context.DrawContext;
import org.lwjgl.system.MemoryUtil;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.polyfrost.polydium.client.perf.SodiumGlIndirect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.polyfrost.polydium.client.perf.SodiumGlIndirect.COMMAND_SIZE;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.gpu.device.batch.GLDrawBatch", remap = false)
public abstract class MixinGlIndirectTerrainBatch implements SodiumGlIndirect.Batch {
    @Unique
    private long polydium$commands;

    @Unique
    private GpuBufferSlice polydium$prepared;

    @Unique
    private long polydium$preparedGeneration;

    @Override
    public long polydium$commands() {
        return polydium$commands;
    }

    @Override
    public int polydium$commandBytes() {
        return ((MultiDrawBatch) (Object) this).size * COMMAND_SIZE;
    }

    @Override
    public void polydium$prepared(GpuBufferSlice commands, long generation) {
        polydium$prepared = commands;
        polydium$preparedGeneration = generation;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void polydium$allocCommands(int capacity, CallbackInfo ci) {
        if (!SodiumGlIndirect.supported()) {
            return;
        }

        long bytes = (long) capacity * COMMAND_SIZE;
        long commands = MemoryUtil.nmemAlignedAlloc(32, bytes);
        MemoryUtil.memSet(commands, 0, bytes);
        for (int i = 0; i < capacity; i++) {
            MemoryUtil.memPutInt(commands + (long) i * COMMAND_SIZE + 4, 1);
        }

        polydium$commands = commands;
    }

    @Inject(method = "put", at = @At("TAIL"))
    private void polydium$putCommand(int size, int elementCount, int baseVertex, long elementOffset, CallbackInfo ci) {
        long commands = polydium$commands;
        if (commands == 0) {
            return;
        }

        long command = commands + (long) size * COMMAND_SIZE;
        MemoryUtil.memPutInt(command, elementCount);
        MemoryUtil.memPutInt(command + 8, (int) elementOffset);
        MemoryUtil.memPutInt(command + 12, baseVertex);
    }

    @Inject(method = "draw", at = @At("HEAD"), cancellable = true)
    private void polydium$drawIndirect(DrawContext context, CallbackInfo ci) {
        SodiumGlIndirect.batchDrawing = true;
        GpuBufferSlice prepared = polydium$prepared;
        polydium$prepared = null;
        long commands = polydium$commands;
        if (commands == 0 || SodiumGlIndirect.irisTessellation || !PolydiumToggles.glIndirectTerrain) {
            return;
        }

        int count = ((MultiDrawBatch) (Object) this).size;
        boolean usable = prepared != null && polydium$preparedGeneration == SodiumGlIndirect.generation()
                && prepared.length() == (long) count * COMMAND_SIZE && !prepared.buffer().isClosed();
        SodiumGlIndirect.countDraw(usable);
        if (!usable) {
            prepared = SodiumGlIndirect.upload(commands, count * COMMAND_SIZE);
        }

        context.getPass().drawIndexedIndirect(prepared, count);
        ci.cancel();
    }

    @Inject(method = "delete", at = @At("TAIL"))
    private void polydium$freeCommands(CallbackInfo ci) {
        if (polydium$commands != 0) {
            MemoryUtil.nmemAlignedFree(polydium$commands);
            polydium$commands = 0;
        }
    }
}
//?}
