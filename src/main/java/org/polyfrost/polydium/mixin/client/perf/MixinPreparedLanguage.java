package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.polyfrost.polydium.client.perf.PreparedLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

@Mixin(LanguageManager.class)
public abstract class MixinPreparedLanguage implements ResourceManagerReloadListener {
    @Unique
    private static final boolean POLYDIUM$AVAILABLE = !FabricLoader.getInstance().isModLoaded("languagereload");

    @Shadow
    private String currentCode;

    @Unique
    private PreparedLanguage polydium$prepared;

    @Shadow
    private static Map<String, LanguageInfo> extractLanguages(Stream<PackResources> resourcePacks) {
        throw new AssertionError();
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.SharedState currentReload, Executor taskExecutor, PreparableReloadListener.PreparationBarrier preparationBarrier, Executor reloadExecutor) {
        if (!POLYDIUM$AVAILABLE) {
            return ResourceManagerReloadListener.super.reload(currentReload, taskExecutor, preparationBarrier, reloadExecutor);
        }
        ResourceManager manager = currentReload.resourceManager();
        String code = this.currentCode;
        return CompletableFuture.supplyAsync(() -> polydium$prepare(manager, code), taskExecutor)
            .thenCompose(preparationBarrier::wait)
            .thenAcceptAsync(prepared -> {
                ProfilerFiller reloadProfiler = Profiler.get();
                reloadProfiler.push("listener");
                this.polydium$prepared = prepared;
                try {
                    this.onResourceManagerReload(manager);
                } finally {
                    this.polydium$prepared = null;
                }
                reloadProfiler.pop();
            }, reloadExecutor);
    }

    @WrapOperation(
        method = "onResourceManagerReload",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/language/LanguageManager;extractLanguages(Ljava/util/stream/Stream;)Ljava/util/Map;")
    )
    private Map<String, LanguageInfo> polydium$usePreparedLanguages(Stream<PackResources> resourcePacks, Operation<Map<String, LanguageInfo>> original) {
        PreparedLanguage prepared = this.polydium$prepared;
        return prepared == null ? original.call(resourcePacks) : prepared.languages();
    }

    @WrapOperation(
        method = "onResourceManagerReload",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/language/ClientLanguage;loadFrom(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Z)Lnet/minecraft/client/resources/language/ClientLanguage;")
    )
    private ClientLanguage polydium$usePreparedTranslations(ResourceManager resourceManager, List<String> languageStack, boolean defaultRightToLeft, Operation<ClientLanguage> original) {
        PreparedLanguage prepared = this.polydium$prepared;
        if (prepared == null || !prepared.languageStack().equals(languageStack) || prepared.defaultRightToLeft() != defaultRightToLeft) {
            return original.call(resourceManager, languageStack, defaultRightToLeft);
        }
        if (prepared.failure() != null) {
            throw prepared.failure();
        }
        return prepared.language();
    }

    @Unique
    private static PreparedLanguage polydium$prepare(ResourceManager manager, String code) {
        Map<String, LanguageInfo> languages = extractLanguages(manager.listPacks());
        List<String> languageStack = new ArrayList<>(2);
        boolean defaultRightToLeft = false;
        languageStack.add("en_us");
        if (!code.equals("en_us")) {
            LanguageInfo currentLanguage = languages.get(code);
            if (currentLanguage != null) {
                languageStack.add(code);
                defaultRightToLeft = currentLanguage.bidirectional();
            }
        }
        try {
            return new PreparedLanguage(languages, List.copyOf(languageStack), defaultRightToLeft, ClientLanguage.loadFrom(manager, languageStack, defaultRightToLeft), null);
        } catch (RuntimeException e) {
            return new PreparedLanguage(languages, List.copyOf(languageStack), defaultRightToLeft, null, e);
        }
    }
}
//?}
