package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.5 {
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerEquipment;
import net.minecraft.world.item.ItemStack;
import org.polyfrost.polydium.client.perf.CullingHead;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityCullingHead implements CullingHead {
    @Shadow
    @Final
    protected EntityEquipment equipment;

    @Unique
    private ItemStack polydium$cullingHead;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void polydium$trackEquipment(CallbackInfo ci) {
        Class<?> equipmentClass = this.equipment.getClass();
        if ((equipmentClass == EntityEquipment.class || equipmentClass == PlayerEquipment.class)
                && this.equipment instanceof CullingHead.Tracker tracker && CullingHead.PLAIN_HEAD_SLOT.get(this.getClass()))
            tracker.polydium$trackFor(this);
    }

    @Override
    public ItemStack polydium$getCullingHead() {
        return this.polydium$cullingHead;
    }

    @Override
    public void polydium$setCullingHead(ItemStack head) {
        this.polydium$cullingHead = head;
    }
}
//?}
