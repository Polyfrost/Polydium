package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontManager;
import org.polyfrost.polydium.client.perf.TextWidthCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FontManager.class)
public abstract class MixinTextWidthCacheManager {
    @ModifyReturnValue(method = {"createFont", "createFontFilterFishy"}, at = @At("RETURN"))
    private Font polydium$enableWidthCache(Font font) {
        if (font.getClass() == Font.class) {
            ((TextWidthCache.Access) font).polydium$enableWidthCache();
        }
        return font;
    }
}
