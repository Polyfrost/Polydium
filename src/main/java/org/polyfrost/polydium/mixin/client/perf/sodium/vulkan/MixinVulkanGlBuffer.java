package org.polyfrost.polydium.mixin.client.perf.sodium.vulkan;

import com.mojang.blaze3d.buffers.GpuBuffer;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlBuffer;
import org.lwjgl.opengl.GL20C;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.polyfrost.polydium.vulkan.sodium.VkBacked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = GlBuffer.class, remap = false)
public class MixinVulkanGlBuffer implements VkBacked {

    @Unique
    private GpuBuffer polydium$gpu;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL20C;glGenBuffers()I"))
    private static int polydium$noGlHandle() {
        return VulkanActive.active ? 0 : GL20C.glGenBuffers();
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
