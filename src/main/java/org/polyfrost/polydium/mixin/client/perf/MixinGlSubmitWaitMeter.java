package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import org.polyfrost.polydium.client.perf.GpuWaitMeter;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = /*? if >= 26.3 {*/"com.mojang.renderpearl.backend.opengl.GlCommandEncoder"/*?} else {*//*"com.mojang.blaze3d.opengl.GlCommandEncoder"*//*?}*/)
public abstract class MixinGlSubmitWaitMeter {
    @WrapMethod(method = "awaitSubmit")
    private boolean polydium$timeSubmitWait(long index, long timeout, Operation<Boolean> original) {
        if (timeout == 0L || Minecraft.getInstance().options.enableVsync().get())
            return original.call(index, timeout);

        GpuWaitMeter.beginWait();
        boolean done = original.call(index, timeout);
        GpuWaitMeter.endWait();

        return done;
    }
}
//?}
