package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.DSL;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.main.Main;
import org.polyfrost.polydium.client.perf.StartupBackground;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Main.class)
public abstract class MixinMainStartupBackground {
    @WrapOperation(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/datafix/DataFixers;optimize(Ljava/util/Set;)Ljava/util/concurrent/CompletableFuture;"))
    private static CompletableFuture<?> polydium$optimizeDataFixersInBackground(Set<DSL.TypeReference> types, Operation<CompletableFuture<?>> original) {
        return StartupBackground.optimizeDataFixers(types, original::call);
    }

    @WrapOperation(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/CrashReport;preload()V"))
    private static void polydium$preloadCrashReportInBackground(Operation<Void> original) {
        StartupBackground.preloadCrashReport();
    }

    @WrapOperation(method = "main", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;join()Ljava/lang/Object;"))
    private static Object polydium$awaitCrashReportPreload(CompletableFuture<?> dataFixers, Operation<Object> original) {
        Object result = original.call(dataFixers);
        StartupBackground.awaitCrashReportPreload();
        return result;
    }
}
