package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import net.minecraft.util.Util;
//?} else
/*import net.minecraft.Util;*/
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Locale;

@Mixin(Util.class)
public abstract class MixinUtilPlatformCache {
    @Unique
    private static final String polydium$osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);

    @Redirect(method = "getPlatform", at = @At(value = "INVOKE", target = "Ljava/lang/String;toLowerCase(Ljava/util/Locale;)Ljava/lang/String;"))
    private static String polydium$cachedOsName(String osName, Locale locale) {
        return polydium$osName;
    }
}
