package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.polyfrost.polydium.client.perf.CullBoxCache;
//? if < 26.3 {
/*import org.polyfrost.polydium.client.perf.EntityCullLerp;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityCullBoxReuse {
    @WrapOperation(method = "shouldRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(D)Lnet/minecraft/world/phys/AABB;"))
    private AABB polydium$reuseCullBox(AABB base, double amount, Operation<AABB> original, @Local(argsOnly = true) Entity entity) {
        //? if < 26.3 {
        /*AABB moved = EntityCullLerp.inflateMoved(base, amount);
        if (moved != null) {
            return moved;
        }
        *///?}
        return ((CullBoxCache) entity).polydium$inflateCullBox(base, amount);
    }
}
//?}
