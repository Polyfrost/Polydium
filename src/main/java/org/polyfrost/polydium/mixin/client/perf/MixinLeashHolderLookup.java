package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public abstract class MixinLeashHolderLookup {
    @WrapOperation(
        method = {"shouldRender", "extractRenderState"},
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Leashable;getLeashHolder()Lnet/minecraft/world/entity/Entity;")
    )
    private Entity polydium$leashHolder(Leashable leashable, Operation<Entity> original) {
        Leashable.LeashData leashData;
        if (!(leashable instanceof Entity entity) || (leashData = leashable.getLeashData()) == null) {
            return original.call(leashable);
        }
        int delayedHolderId = ((LeashDataAccessor) (Object) leashData).polydium$delayedLeashHolderId();
        if (delayedHolderId != 0) {
            Entity holder = entity.level().getEntity(delayedHolderId);
            if (entity.level().isClientSide() && holder != null) {
                leashData.setLeashHolder(holder);
            }
        }
        return leashData.leashHolder;
    }
}
//?}
