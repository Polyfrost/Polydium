package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.4 && < 26.3 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.polyfrost.polydium.client.perf.EntityCullLerp;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityCullLerp {
    @ModifyExpressionValue(method = "getBoundingBoxForCulling", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;"))
    private AABB polydium$lerpCullBox(AABB box, @Local(argsOnly = true) Entity entity) {
        return EntityCullLerp.lerp(entity, box);
    }
}
*///?} else if < 1.21.4 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.polyfrost.polydium.client.perf.EntityCullLerp;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityCullLerp {
    @WrapOperation(method = "shouldRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getBoundingBoxForCulling()Lnet/minecraft/world/phys/AABB;"))
    private AABB polydium$lerpCullBox(Entity entity, Operation<AABB> original) {
        return EntityCullLerp.lerp(entity, original.call(entity));
    }
}
*///?}
