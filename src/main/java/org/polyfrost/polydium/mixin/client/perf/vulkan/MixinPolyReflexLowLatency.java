package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.objectweb.asm.Opcodes;
import org.polyfrost.polydium.client.PolydiumConstants;
import org.polyfrost.polydium.client.perf.PresentThread;
import org.polyfrost.polydium.compat.PolyReflexCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "org.polyfrost.polyreflex.LowLatency", remap = false)
public abstract class MixinPolyReflexLowLatency {

    @ModifyExpressionValue(method = {"beforeSubmit", "beforePresent", "afterPresent", "marker", "antiLagUpdate"}, at = @At(value = "FIELD", target = "Lorg/polyfrost/polyreflex/LowLatency;frameId:J", opcode = Opcodes.GETSTATIC), remap = false, require = 0)
    private static long polydium$presentingFrame(long frameId) {
        return PolyReflexCompat.frameId(frameId, PresentThread.onWorker());
    }

    @ModifyExpressionValue(method = "frameStart", at = @At(value = "CONSTANT", args = "intValue=260"), remap = false, require = 0)
    private static int polydium$unlimitedCutoff(int original) {
        return PolydiumConstants.UNLIMITED_FRAMERATE_CUTOFF;
    }
}
//?}
