package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.mojang.renderpearl.api.textures.GpuSampler;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Supplier;

@Mixin(targets = "net.minecraft.client.renderer.rendertype.RenderSetup$TextureBinding")
public interface RenderSetupBindingAccessor {
    @Invoker("location")
    Identifier polydium$location();

    @Invoker("sampler")
    Supplier<GpuSampler> polydium$sampler();
}
//?}
