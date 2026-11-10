package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.10 {
import net.minecraft.client.Minecraft;
import org.polyfrost.polydium.client.perf.EtfSubmitDataPool;
import org.polyfrost.polydium.client.perf.SubmitPosePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinSubmitPosePoolFrame {
    @Inject(method = "runTick", at = @At("HEAD"))
    private void polydium$recycleSubmitPoses(CallbackInfo ci) {
        SubmitPosePool.recycle();
        EtfSubmitDataPool.recycle();
    }
}
//?}
