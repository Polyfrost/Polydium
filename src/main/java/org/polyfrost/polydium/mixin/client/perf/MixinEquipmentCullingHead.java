package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.5 {
import java.util.EnumMap;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.polyfrost.polydium.client.perf.CullingHead;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityEquipment.class)
public abstract class MixinEquipmentCullingHead implements CullingHead.Tracker {
    @Shadow
    @Final
    private EnumMap<EquipmentSlot, ItemStack> items;

    @Unique
    private CullingHead polydium$owner;

    @Override
    public void polydium$trackFor(CullingHead owner) {
        if (this.polydium$owner != null)
            return;

        this.polydium$owner = owner;
        this.polydium$updateCullingHead();
    }

    @Inject(method = {"setAll", "clear"}, at = @At("RETURN"))
    private void polydium$trackCullingHead(CallbackInfo ci) {
        this.polydium$updateCullingHead();
    }

    @Inject(method = "set", at = @At("RETURN"))
    private void polydium$trackSetCullingHead(EquipmentSlot slot, ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        if (slot == EquipmentSlot.HEAD)
            this.polydium$updateCullingHead();
    }

    @Unique
    private void polydium$updateCullingHead() {
        if (this.polydium$owner == null)
            return;

        ItemStack head = this.items.getOrDefault(EquipmentSlot.HEAD, ItemStack.EMPTY);

        this.polydium$owner.polydium$setCullingHead(head.isEmpty() || head.is(Items.DRAGON_HEAD) ? head : ItemStack.EMPTY);
    }
}
//?}
