package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 && < 26.2 {
/*import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.systems.CommandEncoder;
import net.minecraft.client.renderer.MappableRingBuffer;
import org.polyfrost.polydium.client.perf.FenceBatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MappableRingBuffer.class)
public abstract class MixinFenceBatchRingBuffer {
    @Redirect(method = "rotate", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/CommandEncoder;createFence()Lcom/mojang/blaze3d/buffers/GpuFence;"))
    private GpuFence polydium$deferFence(CommandEncoder encoder) {
        return FenceBatcher.defer();
    }
}
*///?}
