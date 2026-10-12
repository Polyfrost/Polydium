package org.polyfrost.polydium.client.perf;

//? if >= 1.21.1 {
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
//? if < 1.21.4
/*import net.minecraft.util.profiling.ProfilerFiller;*/

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public record PreparedLanguage(Map<String, LanguageInfo> languages, List<String> languageStack, boolean defaultRightToLeft, ClientLanguage language, RuntimeException failure) {
    public interface Access {
        CompletableFuture<Void> polydium$reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager manager, Executor taskExecutor, Executor reloadExecutor/*? if < 1.21.4 {*//*, ProfilerFiller reloadProfiler*//*?}*/);
    }
}
//?}
