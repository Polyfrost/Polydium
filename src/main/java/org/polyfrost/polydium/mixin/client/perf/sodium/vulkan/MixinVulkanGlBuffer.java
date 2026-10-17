package org.polyfrost.polydium.mixin.client.perf.sodium.vulkan;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBuffer;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlBuffer;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.polyfrost.polydium.vulkan.sodium.VkBacked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = GlBuffer.class, remap = false)
public class MixinVulkanGlBuffer implements VkBacked {

    @Unique
    private GpuBuffer polydium$gpu;

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL20C;glGenBuffers()I"))
    private static int polydium$noGlHandle(Operation<Integer> original) {
        return VulkanActive.active ? 0 : original.call();
    }

    @Override
    public GpuBuffer polydium$gpu() {
        return this.polydium$gpu;
    }

    @Override
    public void polydium$gpu(GpuBuffer buffer) {
        this.polydium$gpu = buffer;
    }
}
