package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.device.GpuDevice;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "net.minecraft.client.renderer.StagedVertexBuffer$GpuBufferPool")
public class MixinStagedVertexBufferPool {
    @Unique
    private static final int POLYDIUM_MAX_IDLE_FRAMES = 60;

    @Shadow
    @Final
    private List<GpuBuffer> available;

    @Unique
    private final Reference2IntOpenHashMap<GpuBuffer> polydium$idleFrames = new Reference2IntOpenHashMap<>();
    @Unique
    private final List<GpuBuffer> polydium$kept = new ArrayList<>();

    @Inject(method = "acquire", at = @At("RETURN"))
    private void polydium$resetIdle(GpuDevice device, int minSize, CallbackInfoReturnable<GpuBuffer> cir) {
        polydium$idleFrames.removeInt(cir.getReturnValue());
    }

    @Inject(method = "endFrame", at = @At("HEAD"))
    private void polydium$holdIdleBuffers(GpuDevice device, CallbackInfo ci) {
        for (GpuBuffer buffer : available) {
            int idle = polydium$idleFrames.getInt(buffer) + 1;
            if (idle > POLYDIUM_MAX_IDLE_FRAMES) {
                polydium$idleFrames.removeInt(buffer);
                buffer.close();
            } else {
                polydium$idleFrames.put(buffer, idle);
                polydium$kept.add(buffer);
            }
        }
        available.clear();
    }

    @Inject(method = "endFrame", at = @At("RETURN"))
    private void polydium$restoreIdleBuffers(GpuDevice device, CallbackInfo ci) {
        available.addAll(polydium$kept);
        polydium$kept.clear();
    }
}
//?}
