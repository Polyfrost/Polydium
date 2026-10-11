package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.renderer.GameRenderer;
import org.polyfrost.polydium.client.perf.PerfClock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class MixinWindowMinimizedPoll {
    @Unique
    private long polydium$minimizedTick = -1L;
    @Unique
    private boolean polydium$minimized;

    @WrapOperation(
        method = "extractWindow",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;isIconified()Z")
    )
    private boolean polydium$minimizedOncePerTick(Window window, Operation<Boolean> original) {
        long tick = PerfClock.tick;
        if (tick != this.polydium$minimizedTick) {
            this.polydium$minimizedTick = tick;
            this.polydium$minimized = original.call(window);
        }
        return this.polydium$minimized;
    }
}
//?}
