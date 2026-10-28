package org.polyfrost.polydium.mixin.client.perf.sodium;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import org.polyfrost.polydium.client.perf.LightNibbleView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSection", remap = false)
public abstract class MixinClonedLightView {
    @Redirect(method = "copyLightArray", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/lighting/LayerLightEventListener;getDataLayerData(Lnet/minecraft/core/SectionPos;)Lnet/minecraft/world/level/chunk/DataLayer;", remap = true), remap = false, require = 0)
    private static DataLayer polydium$viewLight(LayerLightEventListener listener, SectionPos pos) {
        LightNibbleView.viewer = Thread.currentThread();
        try {
            return listener.getDataLayerData(pos);
        } finally {
            LightNibbleView.viewer = null;
        }
    }
}
