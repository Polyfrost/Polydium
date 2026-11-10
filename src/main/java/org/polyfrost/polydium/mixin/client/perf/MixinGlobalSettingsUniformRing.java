package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import net.minecraft.client.renderer.GlobalSettingsUniform;
import net.minecraft.client.renderer.MappableRingBuffer;
import org.polyfrost.polydium.client.perf.UniformRing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

@Mixin(GlobalSettingsUniform.class)
public abstract class MixinGlobalSettingsUniformRing {
    @Unique
    private MappableRingBuffer polydium$ring;

    @Unique
    private GpuBuffer polydium$written;

    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/CommandEncoder;writeToBuffer(Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;Ljava/nio/ByteBuffer;)V"))
    private void polydium$writeRing(CommandEncoder encoder, GpuBufferSlice slice, ByteBuffer data, Operation<Void> original) {
        if (this.polydium$ring == null) {
            this.polydium$ring = new MappableRingBuffer(() -> "Global Settings UBO", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, GlobalSettingsUniform.UBO_SIZE);
        }
        this.polydium$written = UniformRing.write(this.polydium$ring, data);
    }

    @ModifyArg(method = "update", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setGlobalSettingsUniform(Lcom/mojang/renderpearl/api/buffers/GpuBuffer;)V"))
    private GpuBuffer polydium$bindRing(GpuBuffer buffer) {
        GpuBuffer written = this.polydium$written;
        this.polydium$written = null;
        return written != null ? written : buffer;
    }

    @Inject(method = "close", at = @At("RETURN"))
    private void polydium$closeRing(CallbackInfo ci) {
        if (this.polydium$ring != null) {
            this.polydium$ring.close();
            this.polydium$ring = null;
        }
    }
}
//?}
