package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.gpu.GPULimits", remap = false)
public abstract class MixinSodiumPerFrameChecksGpuLimits {
    @Unique
    private static Util.OS polydium$platform;

    @WrapOperation(
            method = "getSubTexelPrecisionBits",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getPlatform()Lnet/minecraft/util/Util$OS;")
    )
    private static Util.OS polydium$platformOnce(Operation<Util.OS> original) {
        Util.OS platform = polydium$platform;
        if (platform == null) {
            polydium$platform = platform = original.call();
        }
        return platform;
    }
}
//?}
