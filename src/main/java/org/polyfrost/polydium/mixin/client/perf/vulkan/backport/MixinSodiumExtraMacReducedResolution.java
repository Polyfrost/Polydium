package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.polyfrost.polydium.vulkan.SodiumExtraMac;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "me.flashyreese.mods.sodiumextra.client.util.MacReducedResolution", remap = false)
public class MixinSodiumExtraMacReducedResolution {
    @ModifyReturnValue(method = "shouldReduceFramebuffer()Z", at = @At("RETURN"), remap = false, require = 0)
    private static boolean polydium$onlyHighDpi(boolean original) {
        return original && SodiumExtraMac.window != 0L && SodiumExtraMac.isHighDpi(SodiumExtraMac.window);
    }
}
