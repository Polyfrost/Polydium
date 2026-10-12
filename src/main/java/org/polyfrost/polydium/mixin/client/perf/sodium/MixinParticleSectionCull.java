package org.polyfrost.polydium.mixin.client.perf.sodium;

import org.polyfrost.polydium.client.PolydiumToggles;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.particle.QuadParticleGroup;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import org.polyfrost.polydium.client.perf.ParticleSectionVisibility;
import org.polyfrost.polydium.client.perf.PerfClock;
import org.polyfrost.polydium.compat.Mods;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = QuadParticleGroup.class, priority = 1500)
public abstract class MixinParticleSectionCull {
    // Reads the result instead of wrapping the call: particle_core wraps the same call, and a second wrapper per particle costs more than the cull saves.
    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/culling/Frustum;pointInFrustum(DDD)Z"))
    private boolean polydium$cullHiddenSection(boolean kept, @Local(argsOnly = true) Frustum frustum, @Local SingleQuadParticle particle) {
        if (!kept || !PolydiumToggles.particleSectionCull) {
            return kept;
        }
        ParticleSectionVisibility.Access cached = (Object) particle instanceof ParticleSectionVisibility.Access access ? access : null;
        if (cached != null && cached.polydium$sectionVisibleTick() == PerfClock.tick) {
            return true;
        }
        SodiumWorldRenderer renderer = SodiumWorldRenderer.instanceNullable();
        if (renderer == null) {
            return true;
        }
        AABB box = particle.getBoundingBox();
        double x = (box.minX + box.maxX) * 0.5;
        double y = box.minY;
        double z = (box.minZ + box.maxZ) * 0.5;
        if (renderer.isBoxVisible(x, y, z, x, y, z)) {
            if (cached != null) {
                cached.polydium$sectionVisibleTick(PerfClock.tick);
            }
            return true;
        }
        // particle_core keeps particles whose centre is outside the frustum, where Sodium has no visible section to report.
        return Mods.PARTICLE_CORE && (frustum == null || !frustum.pointInFrustum(x, y, z));
    }
}
//?}
