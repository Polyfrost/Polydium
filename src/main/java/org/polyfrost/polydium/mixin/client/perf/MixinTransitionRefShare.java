package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.2 {
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = EntityRenderState.class, priority = 1100)
public abstract class MixinTransitionRefShare {
}
//?}
