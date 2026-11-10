package org.polyfrost.polydium.mixin.client.perf.sodium.vulkan;

//? if >= 26.1 {
import com.mojang.blaze3d.buffers.GpuBuffer;
import net.caffeinemc.mods.sodium.mixin.core.GlBufferAccessor;
import org.polyfrost.polydium.vulkan.VulkanGpuBuffer;
import org.polyfrost.polydium.vulkan.sodium.SodiumVulkan;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = VulkanGpuBuffer.class, remap = false)
public abstract class MixinVulkanGpuBufferHandle implements GlBufferAccessor {

    @Override
    public int sodium$getHandle() {
        SodiumVulkan.sectionTimeInfo = (GpuBuffer) (Object) this;
        return 0;
    }
}
//?}
