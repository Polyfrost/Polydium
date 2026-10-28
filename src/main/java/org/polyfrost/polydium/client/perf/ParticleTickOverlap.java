package org.polyfrost.polydium.client.perf;

//? if >= 1.21.10 {
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.culling.Frustum;
//? if >= 26.1 {
import net.minecraft.client.renderer.state.level.ParticlesRenderState;
//?} else
/*import net.minecraft.client.renderer.state.ParticlesRenderState;*/
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.lwjgl.system.JNI;
import org.lwjgl.system.Platform;
import org.lwjgl.system.macosx.DynamicLinkLoader;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.function.Supplier;

public final class ParticleTickOverlap {
    public interface Finisher {
        void polydium$finishTick(CompletableFuture<?>[] futures);
    }

    private static Finisher pending;
    private static CompletableFuture<?>[] kept, pendingFutures;
    private static boolean broken;
    private static ParticleEngine extractEngine;
    private static ParticlesRenderState extractState;
    private static Frustum extractFrustum;
    private static Camera extractCamera;
    private static float extractPartialTick;

    private ParticleTickOverlap() {
    }

    private static ForkJoinPool pool;

    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> task) {
        if (!PolydiumToggles.particleTickOverlap)
            return CompletableFuture.supplyAsync(task);
        ForkJoinPool p = pool;
        if (p == null)
            pool = p = new ForkJoinPool(ForkJoinPool.getCommonPoolParallelism(), TickWorker::new, null, false);
        return CompletableFuture.supplyAsync(task, p);
    }

    public static boolean isTickPool(ForkJoinPool p) {
        return p == pool && p != null;
    }

    private static final class TickWorker extends ForkJoinWorkerThread {
        TickWorker(ForkJoinPool pool) {
            super(null, pool, true);
        }

        @Override
        protected void onStart() {
            super.onStart();
            setName("Polydium Particle Tick " + getPoolIndex());
            userInteractiveQos();
        }
    }

    private static final int QOS_CLASS_USER_INTERACTIVE = 0x21;

    private static void userInteractiveQos() {
        if (Platform.get() != Platform.MACOSX)
            return;
        try {
            long fn = DynamicLinkLoader.dlsym(DynamicLinkLoader.RTLD_DEFAULT, "pthread_set_qos_class_self_np");
            if (fn != 0)
                JNI.invokeI(QOS_CLASS_USER_INTERACTIVE, 0, fn);
        } catch (Throwable ignored) {
        }
    }

    public static boolean enabled() {
        return PolydiumToggles.particleTickOverlap && !broken;
    }

    public static boolean pending() {
        return pending != null;
    }

    public static CompletableFuture<Void> allOf(CompletableFuture<?>[] futures, Object engine) {
        if (futures.length == 0 || pending != null || !enabled() || !(engine instanceof Finisher)) {
            kept = null;
            return CompletableFuture.allOf(futures);
        }
        kept = futures;
        return CompletableFuture.completedFuture(null);
    }

    public static <T> Iterator<T> iterator(List<T> futures, Object engine) {
        CompletableFuture<?>[] deferred = kept;
        if (deferred == null)
            return futures.iterator();
        kept = null;
        pendingFutures = deferred;
        pending = (Finisher) engine;
        ProfilerFiller profiler = Profiler.get();
        for (int i = 0; i < deferred.length; i++)
            profiler.pop();
        return Collections.emptyIterator();
    }

    public static void complete() {
        Finisher engine = pending;
        if (engine != null) {
            CompletableFuture<?>[] futures = pendingFutures;
            pending = null;
            pendingFutures = null;
            engine.polydium$finishTick(futures);
        }
    }

    public static boolean deferExtract(ParticleEngine engine, ParticlesRenderState state, Frustum frustum, Camera camera, float partialTick) {
        if (pending == null || !PolydiumToggles.particleTickOverlap)
            return false;
        extractEngine = engine;
        extractState = state;
        extractFrustum = frustum;
        extractCamera = camera;
        extractPartialTick = partialTick;
        return true;
    }

    public static void runDeferredExtract() {
        ParticleEngine engine = extractEngine;
        if (engine != null) {
            ParticlesRenderState state = extractState;
            Frustum frustum = extractFrustum;
            Camera camera = extractCamera;
            clearDeferredExtract();
            engine.extract(state, frustum, camera, extractPartialTick);
        }
    }

    private static void clearDeferredExtract() {
        extractEngine = null;
        extractState = null;
        extractFrustum = null;
        extractCamera = null;
    }

    public static void frameStart() {
        clearDeferredExtract();
        complete();
    }

    public static void fail(Throwable t) {
        broken = true;
        LoggerFactory.getLogger("polydium/perf").error("Deferred Particle Core tick failed; joining it in the tick again", t);
    }
}
//?}
