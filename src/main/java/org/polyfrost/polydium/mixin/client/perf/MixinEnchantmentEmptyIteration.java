package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(EnchantmentHelper.class)
public abstract class MixinEnchantmentEmptyIteration {
    @WrapOperation(
            method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentVisitor;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/ItemEnchantments;entrySet()Ljava/util/Set;")
    )
    private static Set<Object2IntMap.Entry<Holder<Enchantment>>> polydium$skipEmptyEntries(ItemEnchantments enchantments,
                                                                                         Operation<Set<Object2IntMap.Entry<Holder<Enchantment>>>> original) {
        if (enchantments.isEmpty()) {
            return ObjectSets.emptySet();
        }
        return original.call(enchantments);
    }
}
//?}
