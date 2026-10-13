package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
//? if < 1.21.10
/*import net.minecraft.server.packs.resources.ResourceManager;*/
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
//? if < 1.21.4
/*import net.minecraft.util.profiling.ProfilerFiller;*/
import org.polyfrost.polydium.client.perf.PreparedLanguage;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ResourceManagerReloadListener.class)
public interface MixinPreparedLanguageReload {
    //? if >= 1.21.10 {
    @WrapMethod(method = "reload")
    private CompletableFuture<Void> polydium$prepareLanguage(PreparableReloadListener.SharedState currentReload, Executor taskExecutor, PreparableReloadListener.PreparationBarrier preparationBarrier,
                                                             Executor reloadExecutor, Operation<CompletableFuture<Void>> original) {
        if (this instanceof PreparedLanguage.Access language) {
            return language.polydium$reload(preparationBarrier, currentReload.resourceManager(), taskExecutor, reloadExecutor);
        }
        return original.call(currentReload, taskExecutor, preparationBarrier, reloadExecutor);
    }
    //?} else if >= 1.21.4 {
    /*@WrapMethod(method = "reload")
    private CompletableFuture<Void> polydium$prepareLanguage(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager manager, Executor taskExecutor, Executor reloadExecutor,
                                                             Operation<CompletableFuture<Void>> original) {
        if (this instanceof PreparedLanguage.Access language) {
            return language.polydium$reload(preparationBarrier, manager, taskExecutor, reloadExecutor);
        }
        return original.call(preparationBarrier, manager, taskExecutor, reloadExecutor);
    }
    *///?} else {
    /*@WrapMethod(method = "reload")
    private CompletableFuture<Void> polydium$prepareLanguage(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager manager, ProfilerFiller prepareProfiler,
                                                             ProfilerFiller reloadProfiler, Executor taskExecutor, Executor reloadExecutor, Operation<CompletableFuture<Void>> original) {
        if (this instanceof PreparedLanguage.Access language) {
            return language.polydium$reload(preparationBarrier, manager, taskExecutor, reloadExecutor, reloadProfiler);
        }
        return original.call(preparationBarrier, manager, prepareProfiler, reloadProfiler, taskExecutor, reloadExecutor);
    }
    *///?}
}
//?}
