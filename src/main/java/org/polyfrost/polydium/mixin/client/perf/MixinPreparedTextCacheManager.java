package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontManager;
import org.polyfrost.polydium.client.perf.PreparedTextCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FontManager.class)
public abstract class MixinPreparedTextCacheManager {
    @ModifyReturnValue(method = {"createFont", "createFontFilterFishy"}, at = @At("RETURN"))
    private Font polydium$enablePreparedTextCache(Font font) {
        if (font.getClass() == Font.class) {
            ((PreparedTextCache.Access) font).polydium$enablePreparedTextCache();
        }
        return font;
    }
}
//?}
