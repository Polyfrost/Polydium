package org.polyfrost.polydium.mixin.client.perf.xaero;

//? if xaerominimap && < 1.21.6 {
/*import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.GuiGraphics;
import org.polyfrost.polydium.compat.XaeroMinimapHudCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.graphics.ImprovedFramebuffer;
import xaero.hud.compat.mods.ImmediatelyFastHelper;

import java.util.Objects;

@Pseudo
@Mixin(targets = "xaero.common.minimap.render.MinimapFBORenderer", remap = false)
public class MixinMinimapFBORenderer {
    @Shadow
    private ImprovedFramebuffer rotationFramebuffer;

    @Unique
    private final XaeroMinimapHudCache polydium$hudCache = new XaeroMinimapHudCache();

    @Inject(method = "renderChunksToFBO", at = @At("HEAD"), cancellable = true, remap = false)
    private void polydium$reuseFramebuffer(
            CallbackInfo ci,
            @Local(argsOnly = true) GuiGraphics guiGraphics,
            @Local(argsOnly = true, ordinal = 0) int viewW,
            @Local(argsOnly = true, ordinal = 2) int shape,
            @Local(argsOnly = true, ordinal = 1) boolean lockedNorth,
            @Local(argsOnly = true, ordinal = 2) boolean cave
    ) {
        int key = Objects.hash(System.identityHashCode(rotationFramebuffer), viewW, shape, lockedNorth, cave);
        if (!polydium$hudCache.reuse(key)) return;
        guiGraphics.flush();
        ImmediatelyFastHelper.triggerBatchingBuffersFlush(guiGraphics);
        ci.cancel();
    }
}
*///?}
