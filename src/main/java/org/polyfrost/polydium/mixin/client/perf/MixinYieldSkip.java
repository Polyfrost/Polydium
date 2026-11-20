package org.polyfrost.polydium.mixin.client.perf;

//? if < 26.1 {
/*import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Ported from Canvas https://raw.githubusercontent.com/vram-guild/canvas/refs/heads/1.19/LICENSE
@Mixin(Minecraft.class)
public abstract class MixinYieldSkip {
    @WrapWithCondition(method = "runTick", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;yield()V"))
    private boolean polydium$skipYield() {
        return false;
    }
}
*///?}
