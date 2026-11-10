package org.polyfrost.polydium.mixin.client;

import net.minecraft.client.gui.screens.AlertScreen;
import org.polyfrost.polydium.client.VulkanPrompt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AlertScreen.class)
public abstract class MixinAlertScreen {
    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/MultiLineLabel;create(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;I)Lnet/minecraft/client/gui/components/MultiLineLabel;"), index = 2)
    private int polydium$capWidth(int width) {
        return (Object) this instanceof VulkanPrompt.Notice ? Math.min(width, VulkanPrompt.Notice.MAX_WIDTH) : width;
    }
}
