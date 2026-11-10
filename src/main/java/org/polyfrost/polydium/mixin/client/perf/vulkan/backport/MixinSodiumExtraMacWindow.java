package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.mojang.blaze3d.platform.Window;
import org.polyfrost.polydium.vulkan.SodiumExtraMac;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class MixinSodiumExtraMacWindow {
    @Shadow @Final private long handle;

    @Inject(method = {"refreshFramebufferSize", "onFramebufferResize"}, at = @At("HEAD"))
    private void polydium$rememberWindow(CallbackInfo ci) {
        SodiumExtraMac.window = this.handle;
    }
}
