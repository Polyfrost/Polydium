package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.sounds.SoundEngine;
import org.polyfrost.polydium.client.perf.AudioDeviceReuse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundEngine.class)
public abstract class MixinAudioDeviceReuse {
    @WrapMethod(method = "reload")
    private void polydium$reuseDeviceDuringReload(Operation<Void> original) {
        AudioDeviceReuse.reloading = AudioDeviceReuse.AVAILABLE && !AudioDeviceReuse.deviceChanging;
        try {
            original.call();
        } finally {
            AudioDeviceReuse.reloading = false;
        }
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundEngine;reload()V"))
    private void polydium$reopenOnDeviceChange(SoundEngine instance, Operation<Void> original) {
        AudioDeviceReuse.deviceChanging = true;
        try {
            original.call(instance);
        } finally {
            AudioDeviceReuse.deviceChanging = false;
        }
    }
}
//?}
