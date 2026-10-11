package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import org.polyfrost.polydium.client.perf.GpuWaitMeter;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = /*? if >= 26.3 {*/"com.mojang.renderpearl.backend.opengl.GlSurface"/*?} else {*//*"com.mojang.blaze3d.opengl.GlSurface"*//*?}*/)
public abstract class MixinGlPresentWaitMeter {
    @WrapMethod(method = "present")
    private void polydium$timeSwap(Operation<Void> original) {
        if (Minecraft.getInstance().options.enableVsync().get()) {
            original.call();
            return;
        }
        GpuWaitMeter.beginWait();
        original.call();
        GpuWaitMeter.endWait();
    }
}
//?}
