package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.3 {
import net.caffeinemc.mods.sodium.client.gpu.device.context.DrawContext;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.polyfrost.polydium.client.perf.SodiumGlIndirect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.gpu.device.batch.MultiDrawBatch", remap = false)
public abstract class MixinGlIndirectTerrainQueue {
    @Inject(method = "prepare", at = @At("HEAD"))
    private void polydium$queuePrepare(DrawContext context, CallbackInfo ci) {
        if ((Object) this instanceof SodiumGlIndirect.Batch batch && PolydiumToggles.glIndirectTerrain && SodiumGlIndirect.canPrepare()) {
            SodiumGlIndirect.queue(batch);
        }
    }
}
//?}
