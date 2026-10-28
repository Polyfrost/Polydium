package org.polyfrost.polydium.mixin.client.perf.particle_core;

//? if >= 1.21.10 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TrackingEmitter;
import net.minecraft.core.particles.ParticleLimit;
import org.polyfrost.polydium.client.perf.ParticleTickOverlap;
//? if >= 26.1
import org.polyfrost.polydium.client.perf.ParticleLightBatch;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

@Mixin(value = ParticleEngine.class, priority = 200002)
public abstract class MixinParticleTickOverlap implements ParticleTickOverlap.Finisher {
    @Shadow @Final private Queue<TrackingEmitter> trackingEmitters;
    @Shadow @Final private Queue<Particle> particlesToAdd;
    @Shadow @Final private Map<ParticleRenderType, ParticleGroup<?>> particles;

    @Shadow
    private ParticleGroup<?> createParticleGroup(ParticleRenderType type) {
        throw new AssertionError();
    }

    //? if >= 26.2
    @Shadow protected abstract void updateCount(ParticleLimit limit, int change);

    @Unique private static MethodHandle polydium$finalize;

    @Inject(method = {"tick", "extract", "setLevel", "clearParticles"}, at = @At("HEAD"), order = 900)
    private void polydium$finishPendingTick(CallbackInfo ci) {
        ParticleTickOverlap.complete();
    }

    @Inject(method = "countParticles", at = @At("HEAD"))
    private void polydium$finishBeforeCount(CallbackInfoReturnable<String> cir) {
        ParticleTickOverlap.complete();
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Queue;isEmpty()Z", remap = false))
    private boolean polydium$deferTail(Queue<?> queue, Operation<Boolean> original) {
        return ParticleTickOverlap.pending() || original.call(queue);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void polydium$finishTick(CompletableFuture<?>[] futures) {
        try {
            CompletableFuture.allOf(futures).join();
            MethodHandle finalize = polydium$finalize;
            if (finalize == null) {
                Class<?> results = Class.forName("me.fzzyhmstrs.particle_core.interfaces.TickResult$Results", false, ParticleEngine.class.getClassLoader());
                polydium$finalize = finalize = MethodHandles.lookup().findVirtual(ParticleEngine.class, "finalizeParticles", MethodType.methodType(void.class, results));
            }
            for (CompletableFuture<?> future : futures)
                finalize.invoke((ParticleEngine) (Object) this, future.join());
        } catch (Throwable t) {
            ParticleTickOverlap.fail(t);
        }

        if (!trackingEmitters.isEmpty()) {
            List<TrackingEmitter> removed = new ArrayList<>();
            for (TrackingEmitter emitter : trackingEmitters) {
                emitter.tick();
                if (!emitter.isAlive())
                    removed.add(emitter);
            }
            trackingEmitters.removeAll(removed);
        }

        Particle particle;
        while ((particle = particlesToAdd.poll()) != null) {
            ParticleGroup group = particles.computeIfAbsent(particle.getGroup(), this::createParticleGroup);
            //? if >= 26.2 {
            if (!group.add(particle))
                particle.getParticleLimit().ifPresent(limit -> updateCount(limit, -1));
            //?} else
            /*group.add(particle);*/
        }

        //? if >= 26.1
        ParticleLightBatch.drain();
    }
}
//?}
