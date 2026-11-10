package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
//? if >= 26.3 {
import com.mojang.renderpearl.api.device.DeviceFeatures;
import com.mojang.renderpearl.api.device.DeviceInfo;
//?} else {
/*import com.mojang.blaze3d.systems.DeviceFeatures;
import com.mojang.blaze3d.systems.DeviceInfo;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Locale;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.gpu.arena.staging.MojangStagingBuffer", remap = false)
public abstract class MixinSodiumIntelGlStaging {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = /*? if >= 26.3 {*/"Lcom/mojang/renderpearl/api/device/DeviceFeatures;persistentMapping()Z"/*?} else {*//*"Lcom/mojang/blaze3d/systems/DeviceFeatures;persistentMapping()Z"*//*?}*/))
    private boolean polydium$noMappedStagingOnIntelGl(DeviceFeatures features, Operation<Boolean> original) {
        DeviceInfo info = RenderSystem.getDevice().getDeviceInfo();
        if ("OpenGL".equals(info.backendName()) && info.vendorName().toLowerCase(Locale.ROOT).contains("intel"))
            return false;
        return original.call(features);
    }
}
//?}
