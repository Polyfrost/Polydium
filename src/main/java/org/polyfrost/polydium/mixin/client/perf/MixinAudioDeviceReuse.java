package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.sounds.SoundEngine;
import org.polyfrost.polydium.client.perf.AudioDeviceReuse;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SoundEngine.class)
public abstract class MixinAudioDeviceReuse {
    @WrapMethod(method = "reload")
    private void polydium$reuseDeviceDuringReload(Operation<Void> original) {
        AudioDeviceReuse.reloading = AudioDeviceReuse.AVAILABLE;
        try {
            original.call();
        } finally {
            AudioDeviceReuse.reloading = false;
        }
    }
}
//?}
