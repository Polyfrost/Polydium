package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.5 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.polyfrost.polydium.client.perf.CullingHead;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingRendererCullingHead {
    @WrapOperation(method = /*? if >= 26.3 {*/"getBoundingBoxForCulling(Lnet/minecraft/world/entity/LivingEntity;F)Lnet/minecraft/world/phys/AABB;"/*?} else {*//*"getBoundingBoxForCulling(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/phys/AABB;"*//*?}*/, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack polydium$cullingHead(LivingEntity entity, EquipmentSlot slot, Operation<ItemStack> original) {
        if (slot == EquipmentSlot.HEAD && entity instanceof CullingHead owner) {
            ItemStack head = owner.polydium$getCullingHead();
            if (head != null)
                return head;
        }
        return original.call(entity, slot);
    }
}
//?}
