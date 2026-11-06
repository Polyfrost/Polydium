package org.polyfrost.polydium.mixin.client.perf.xaero;

//? if xaerominimap {
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import org.polyfrost.polydium.compat.XaeroRadarIconCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.common.icon.XaeroIcon;
import xaero.hud.minimap.radar.icon.RadarIconManager;

@Pseudo
@Mixin(targets = "xaero.hud.minimap.radar.icon.RadarIconManager", remap = false)
public class MixinRadarIconManager {
    @Unique
    private final XaeroRadarIconCache polydium$icons = new XaeroRadarIconCache();

    @Inject(method = "get*", at = @At("HEAD"), cancellable = true, remap = false)
    private void polydium$cachedIcon(CallbackInfoReturnable<XaeroIcon> cir, @Local(argsOnly = true) Entity entity) {
        Object icon = polydium$icons.get(entity.getId());
        if (icon != null) cir.setReturnValue((XaeroIcon) icon);
    }

    @Inject(method = "get*", at = @At("RETURN"), remap = false)
    private void polydium$storeIcon(CallbackInfoReturnable<XaeroIcon> cir, @Local(argsOnly = true) Entity entity) {
        if (cir.getReturnValue() != RadarIconManager.DOT) polydium$icons.put(entity.getId(), cir.getReturnValue());
    }

    @Inject(method = "reset", at = @At("HEAD"), remap = false)
    private void polydium$clearIcons(CallbackInfo ci) {
        polydium$icons.clear();
    }
}
//?}
