package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import com.bawnorton.mixinsquared.TargetHandler;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LevelRenderer.class, priority = 1500)
public abstract class MixinBobbySkyFog {
    @TargetHandler(mixin = "de.johni0702.minecraft.bobby.mixin.LevelRendererMixin", name = "clampMaxValue")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true, require = 0, expect = 0)
    private void polydium$keepFogWhenUnwritten(GpuBufferSlice fogBuffer, CallbackInfoReturnable<GpuBufferSlice> cir) {
        if (Minecraft.getInstance().options.getEffectiveRenderDistance() < 32) {
            cir.setReturnValue(fogBuffer);
        }
    }
}
//?}
