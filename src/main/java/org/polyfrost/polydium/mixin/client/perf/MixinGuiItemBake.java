package org.polyfrost.polydium.mixin.client.perf;

import org.polyfrost.polydium.client.PolydiumToggles;

//? if >= 1.21.8 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.render.GuiRenderer;
import org.polyfrost.polydium.client.perf.GuiItemBake;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiRenderer.class)
public class MixinGuiItemBake {
    @WrapMethod(method = "prepareItemElements")
    private void polydium$bakeItems(Operation<Void> original) {
        if (!PolydiumToggles.guiItemBake) {
            original.call();
            return;
        }
        GuiItemBake.begin();
        try {
            original.call();
        } finally {
            GuiItemBake.end();
        }
    }
}
//?}
