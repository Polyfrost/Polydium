package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import net.minecraft.client.Minecraft;
import org.lwjgl.system.MemoryStack;
import org.polyfrost.polydium.client.perf.RenderThreadStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinRenderThreadStackCapture {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void polydium$captureRenderThreadStack(CallbackInfo ci) {
        RenderThreadStack.capture(MemoryStack.stackGet());
    }
}
//?}
