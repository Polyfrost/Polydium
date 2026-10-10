package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.polyfrost.polydium.client.perf.EntityCheckOrder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = EntityRenderer.class, priority = 1500)
public abstract class MixinEntityCheckOrder {
    @ModifyReturnValue(method = "shouldRender", at = @At("RETURN"))
    //? if >= 26.3 {
    private boolean polydium$sodiumTestLast(boolean original, @Local(argsOnly = true) Entity entity, @Local(argsOnly = true) float partialTicks) {
    //?} else {
    /*private boolean polydium$sodiumTestLast(boolean original, @Local(argsOnly = true) Entity entity) {
        float partialTicks = 0.0F;
    *///?}
        if ((Object) SodiumWorldRenderer.instanceNullable() instanceof EntityCheckOrder order) {
            return order.polydium$finishEntityCheck(original, (EntityRenderer<?, ?>) (Object) this, entity, partialTicks);
        }
        return original;
    }
}
//?}
