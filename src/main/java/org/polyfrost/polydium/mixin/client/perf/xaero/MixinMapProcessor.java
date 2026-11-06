package org.polyfrost.polydium.mixin.client.perf.xaero;

//? if xaeroworldmap {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.polyfrost.polydium.compat.XaeroRegionScanGate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.region.LeveledRegion;

@Pseudo
@Mixin(targets = "xaero.map.MapProcessor", remap = false)
public class MixinMapProcessor {
    @Unique
    private final XaeroRegionScanGate polydium$gate = new XaeroRegionScanGate();

    @WrapOperation(method = "onRenderProcess", at = @At(value = "INVOKE", target = "Lxaero/map/region/LeveledRegion;shouldBeProcessed()Z"), remap = false)
    private boolean polydium$skipIdleRegion(LeveledRegion<?> region, Operation<Boolean> original) {
        return original.call(region) && (!PolydiumToggles.xaeroWorldMapRegionThrottle || polydium$gate.scan(region));
    }

    @ModifyExpressionValue(method = "onRenderProcess", at = @At(value = "INVOKE", target = "Lxaero/map/region/texture/RegionTexture;isUploaded()Z"), remap = false)
    private boolean polydium$trackPending(boolean uploaded) {
        if (!uploaded) polydium$gate.markPending();
        return uploaded;
    }

    @Inject(method = "onRenderProcess", at = @At(value = "INVOKE", target = "Lxaero/map/graphics/TextureUploader;uploadTextures()V"), remap = false)
    private void polydium$endPass(CallbackInfo ci) {
        polydium$gate.endPass();
    }
}
//?}
