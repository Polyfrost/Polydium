package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderSetup.class)
public interface RenderSetupOitAccessor {
    @Accessor("oitPipelineSet")
    OitPipelineSet polydium$oitPipelineSet();
}
//?}
