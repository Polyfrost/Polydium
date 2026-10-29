package org.polyfrost.polydium.mixin.client.perf.lambdynlights;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import org.polyfrost.polydium.client.perf.DynamicLightSourceAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.lambdaurora.lambdynlights.engine.lookup.SpatialLookupEntityEntry", remap = false)
public abstract class MixinLightEntryPosition {
    @Unique
    private Object polydium$source;
    @Unique
    private double polydium$x, polydium$y, polydium$z;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false, require = 0)
    private void polydium$copyPosition(int cellKey, @Coerce Object source, CallbackInfo ci) {
        polydium$source = source;
        polydium$x = DynamicLightSourceAccess.x(source);
        polydium$y = DynamicLightSourceAccess.y(source);
        polydium$z = DynamicLightSourceAccess.z(source);
    }

    @WrapMethod(method = "getDynamicLightLevel", remap = false, require = 0)
    private double polydium$inline$copiedPositionLight(BlockPos pos, Operation<Double> original) {
        Object source = polydium$source;
        if (source == null)
            return original.call(pos);
        double dx = pos.getX() - polydium$x + 0.5;
        double dy = pos.getY() - polydium$y + 0.5;
        double dz = pos.getZ() - polydium$z + 0.5;
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        if (distanceSquared > 60.0625)
            return 0.0;
        int luminance = DynamicLightSourceAccess.luminance(source);
        return luminance > 0 ? luminance - Math.sqrt(distanceSquared) / 7.75 * 15.0 : 0.0;
    }
}
