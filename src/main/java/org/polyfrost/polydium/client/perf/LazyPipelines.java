package org.polyfrost.polydium.client.perf;

//? if >= 1.21.5 {
//? if >= 26.3 {
import com.mojang.blaze3d.pipeline.PipelineCache;
//?} else {
/*import com.mojang.blaze3d.systems.RenderSystem;
*///?}
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import org.polyfrost.polydium.mixin.client.perf.PipelineCacheAccessor;
//? if >= 26.1
import org.polyfrost.polydium.mixin.client.perf.RenderSystemPipelineCacheAccessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public final class LazyPipelines {
    private static volatile List<RenderPipeline> remembered = List.of();

    private LazyPipelines() {
    }

    public static void remember() {
        //? if >= 26.3 {
        PipelineCache cache = RenderSystemPipelineCacheAccessor.polydium$currentPipelineCache();
        if (cache == null) {
        //?} else if >= 26.1 {
        /*if (!(((RenderSystemPipelineCacheAccessor) RenderSystem.getDevice()).polydium$backend() instanceof PipelineCacheAccessor cache)) {
        *///?} else {
        /*if (!(RenderSystem.getDevice() instanceof PipelineCacheAccessor cache)) {
        *///?}
            remembered = List.of();
            return;
        }
        Set<RenderPipeline> registered = Collections.newSetFromMap(new IdentityHashMap<>());
        //? if >= 26.3 {
        registered.addAll(RenderPipelines.requiredPipelines());
        registered.addAll(RenderPipelines.optionalPipelines());
        //?} else {
        /*registered.addAll(RenderPipelines.getStaticPipelines());
        *///?}
        List<RenderPipeline> lazy = new ArrayList<>();
        for (RenderPipeline pipeline : ((PipelineCacheAccessor) cache).polydium$cache().keySet()) {
            if (!registered.contains(pipeline)) {
                lazy.add(pipeline);
            }
        }
        remembered = lazy;
    }

    public static void forget() {
        remembered = List.of();
    }

    public static List<RenderPipeline> withRemembered(List<RenderPipeline> optional) {
        List<RenderPipeline> lazy = remembered;
        remembered = List.of();
        if (lazy.isEmpty()) {
            return optional;
        }
        List<RenderPipeline> pipelines = new ArrayList<>(optional.size() + lazy.size());
        pipelines.addAll(optional);
        pipelines.addAll(lazy);
        return pipelines;
    }
}
//?}
