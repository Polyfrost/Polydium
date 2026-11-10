package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.10 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelLoadTracker.class)
public abstract class MixinLevelLoadCloseDelay {
    @ModifyExpressionValue(method = "isLevelReady", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/LevelLoadTracker;closeDelayMs:J"))
    private long polydium$skipCloseDelay(long delay) {
        return PolydiumToggles.hideLevelLoadingScreen ? 0L : delay;
    }
}
//?}
