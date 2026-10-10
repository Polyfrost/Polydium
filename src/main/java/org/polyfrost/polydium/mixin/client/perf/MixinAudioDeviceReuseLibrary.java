package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if >= 26.1 {
import com.mojang.blaze3d.audio.DeviceList;
//?}
import com.mojang.blaze3d.audio.Library;
import org.lwjgl.openal.ALC10;
import org.polyfrost.polydium.client.perf.AudioDeviceReuse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(Library.class)
public abstract class MixinAudioDeviceReuseLibrary {
    @Shadow
    private long currentDevice;

    @Shadow
    private long context;

    @Unique
    private boolean polydium$deviceKept;

    @Unique
    private boolean polydium$opened;
    @Unique
    private String polydium$openedPreferredDevice;
    @Unique
    //? if >= 26.1 {
    private DeviceList polydium$openedDevices;
    //?} else {
    /*private String polydium$openedDevices;
    *///?}
    @Unique
    private boolean polydium$openedHrtf;

    @Shadow
    public abstract boolean isCurrentDeviceDisconnected();

    @WrapMethod(method = "cleanup")
    private void polydium$keepDevice(Operation<Void> original) {
        this.polydium$deviceKept = AudioDeviceReuse.reloading && this.polydium$opened && !this.isCurrentDeviceDisconnected();
        original.call();
    }

    @WrapOperation(method = "cleanup", at = @At(value = "INVOKE", target = "Lorg/lwjgl/openal/ALC10;alcDestroyContext(J)V"))
    private void polydium$keepContext(long context, Operation<Void> original) {
        if (!this.polydium$deviceKept) {
            original.call(context);
        }
    }

    @WrapOperation(method = "cleanup", at = @At(value = "INVOKE", target = "Lorg/lwjgl/openal/ALC10;alcCloseDevice(J)Z"))
    private boolean polydium$keepOpenDevice(long device, Operation<Boolean> original) {
        return this.polydium$deviceKept || original.call(device);
    }

    //? if >= 26.1 {
    @WrapMethod(method = "init")
    private void polydium$reuseDevice(String preferredDevice, DeviceList currentDevices, boolean useHrtf, Operation<Void> original) {
    //?} else {
    /*@WrapMethod(method = "init")
    private void polydium$reuseDevice(String preferredDevice, boolean useHrtf, Operation<Void> original) {
        String currentDevices = Library.getDefaultDeviceName();
    *///?}
        if (this.polydium$deviceKept) {
            this.polydium$deviceKept = false;
            if (Objects.equals(preferredDevice, this.polydium$openedPreferredDevice) && Objects.equals(currentDevices, this.polydium$openedDevices) && useHrtf == this.polydium$openedHrtf) {
                return;
            }
            ALC10.alcDestroyContext(this.context);
            if (this.currentDevice != 0L) {
                ALC10.alcCloseDevice(this.currentDevice);
            }
        }
        this.polydium$opened = false;
        //? if >= 26.1 {
        original.call(preferredDevice, currentDevices, useHrtf);
        //?} else {
        /*original.call(preferredDevice, useHrtf);
        *///?}
        this.polydium$opened = true;
        this.polydium$openedPreferredDevice = preferredDevice;
        this.polydium$openedDevices = currentDevices;
        this.polydium$openedHrtf = useHrtf;
    }
}
//?}
