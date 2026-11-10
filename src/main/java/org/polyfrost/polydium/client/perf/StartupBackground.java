package org.polyfrost.polydium.client.perf;

import com.mojang.datafixers.DSL;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.CrashReport;
import net.minecraft.ReportType;
import net.minecraft.util.MemoryReserve;

public final class StartupBackground {
    private static volatile CompletableFuture<Void> crashReportPreload;

    private StartupBackground() {
    }

    public static CompletableFuture<?> optimizeDataFixers(Set<DSL.TypeReference> types, Function<Set<DSL.TypeReference>, CompletableFuture<?>> optimize) {
        CompletableFuture<CompletableFuture<?>> started = new CompletableFuture<>();

        Thread.ofPlatform().daemon().name("Polydium DataFixers").start(() -> {
            try {
                started.complete(optimize.apply(types));
            } catch (Throwable throwable) {
                started.completeExceptionally(throwable);
                throw throwable;
            }
        });
        return started.thenCompose(optimized -> optimized.thenApply(result -> result));
    }

    public static void preloadCrashReport() {
        MemoryReserve.allocate();
        CompletableFuture<Void> done = new CompletableFuture<>();
        crashReportPreload = done;
        Thread.ofPlatform().daemon().name("Polydium Crash Report Preload").start(() -> {
            try {
                new CrashReport("Don't panic!", new Throwable()).getFriendlyReport(ReportType.CRASH);
                done.complete(null);
            } catch (Throwable throwable) {
                done.completeExceptionally(throwable);
            }
        });
    }

    public static void awaitCrashReportPreload() {
        CompletableFuture<Void> done = crashReportPreload;
        if (done != null) {
            crashReportPreload = null;
            done.join();
        }
    }
}
