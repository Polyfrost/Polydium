package org.polyfrost.polydium.mixin.client.perf;

//? if < 1.21.10 {
/*import com.google.common.collect.ImmutableList;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import org.polyfrost.polydium.client.perf.OpaqueParticles;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ParticleEngine.class)
public abstract class MixinOpaqueParticleRenderOrder {
    @Shadow
    @Final
    @Mutable
    private static List<ParticleRenderType> RENDER_ORDER;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void polydium$addOpaqueTerrainSheet(CallbackInfo ci) {
        RENDER_ORDER = ImmutableList.<ParticleRenderType>builder().add(OpaqueParticles.TERRAIN_SHEET).addAll(RENDER_ORDER).build();
    }
}
*///?}
