package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.systems.RenderSystem;
import net.caffeinemc.mods.sodium.client.gpu.device.backend.DrawBackend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.gpu.device.backend.DrawBackend", remap = false)
public abstract class MixinIndirectTerrainDraws {
    @ModifyReturnValue(method = "chooseBackend", at = @At("RETURN"))
    private static DrawBackend polydium$preferIndirect(DrawBackend backend) {
        if (backend == DrawBackend.VK_MULTIDRAW && RenderSystem.getDevice().getDeviceInfo().features().multiDrawIndirect()) {
            return DrawBackend.VK_INDIRECT;
        }
        return backend;
    }
}
//?}
