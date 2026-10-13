package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.4 && < 26.3 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.AABB;
import org.polyfrost.polydium.client.perf.EntityCullLerp;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DisplayRenderer.class)
public abstract class MixinDisplayCullLerp {
    @ModifyExpressionValue(method = "getBoundingBoxForCulling(Lnet/minecraft/world/entity/Display;)Lnet/minecraft/world/phys/AABB;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Display;getBoundingBoxForCulling()Lnet/minecraft/world/phys/AABB;"))
    private AABB polydium$lerpCullBox(AABB box, @Local(argsOnly = true) Display entity) {
        return EntityCullLerp.lerp(entity, box);
    }
}
*///?}
