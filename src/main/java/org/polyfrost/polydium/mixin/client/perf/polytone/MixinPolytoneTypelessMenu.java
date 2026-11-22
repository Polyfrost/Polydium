package org.polyfrost.polydium.mixin.client.perf.polytone;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
//? if >= 1.21.11 && < 26.1 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.injection.At;
*///?}

@Pseudo
@Mixin(targets = "net.mehvahdjukaar.polytone.content.slotify.GuiModifierManager", remap = false)
public abstract class MixinPolytoneTypelessMenu {
    //? if >= 1.21.11 && < 26.1 {
    /*@WrapOperation(
            method = {
                    "getSlotModifiers(Lnet/minecraft/class_1703;Lnet/minecraft/class_1735;)Ljava/util/Collection;",
                    "getSlotModifiers(Lnet/minecraft/class_465;Lnet/minecraft/class_1735;)Ljava/util/Collection;",
                    "getScreenModifier(Lnet/minecraft/class_465;)Lnet/mehvahdjukaar/polytone/content/slotify/ScreenModifier;"
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1703;method_17358()Lnet/minecraft/class_3917;", remap = false),
            remap = false, require = 0)
    private MenuType<?> polydium$skipTypelessMenu(AbstractContainerMenu menu, Operation<MenuType<?>> original) {
        Class<?> type = menu.getClass();
        if (type == InventoryMenu.class || type == CreativeModeInventoryScreen.ItemPickerMenu.class) {
            return null;
        }
        return original.call(menu);
    }
    *///?}
}
