package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(PreparedRenderType.class)
public class MixinOitlessDrawGuard {
    @Unique
    private static final Logger polydium$LOGGER = LogUtils.getLogger();

    @Unique
    private static final Set<String> polydium$warned = ConcurrentHashMap.newKeySet();

    @Shadow
    @Final
    private String name;

    @Shadow
    @Final
    private @Nullable OitPipelineSet oitPipelineSet;

    @Inject(method = "drawFromBufferOit", at = @At("HEAD"), cancellable = true)
    private void polydium$skipWithoutOitPipelines(CallbackInfo ci) {
        if (this.oitPipelineSet != null)
            return;

        if (polydium$warned.add(this.name))
            polydium$LOGGER.warn("Render type {} has no OIT pipelines; skipping its draws while improved transparency is on", this.name);
        ci.cancel();
    }
}
//?}
