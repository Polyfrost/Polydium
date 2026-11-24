package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderType.class)
public interface RenderTypeSetupAccessor {
    @Accessor("state")
    RenderSetup polydium$state();
}
//?}
