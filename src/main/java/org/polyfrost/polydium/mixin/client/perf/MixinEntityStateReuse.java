package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.10 {
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.polyfrost.polydium.client.perf.EntityStateReuse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public class MixinEntityStateReuse {
    @Redirect(
        method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;createRenderState()Lnet/minecraft/client/renderer/entity/state/EntityRenderState;"),
        require = 0
    )
    private EntityRenderState polydium$reusedState(EntityRenderer<?, ?> renderer, Entity entity, float partialTicks) {
        return EntityStateReuse.create(renderer, entity);
    }
}
//?}
