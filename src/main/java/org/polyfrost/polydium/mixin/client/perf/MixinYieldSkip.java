package org.polyfrost.polydium.mixin.client.perf;

//? if < 26.1 {
/*import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Ported from Argentum with permission from rdh
@Mixin(Minecraft.class)
public abstract class MixinYieldSkip {
    @WrapWithCondition(method = "runTick", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;yield()V"))
    private boolean polydium$skipYield() {
        return false;
    }
}
*///?}
