package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
//? if >= 26.3
import com.mojang.blaze3d.pipeline.PipelineCache;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

//? if >= 26.3 {
@Mixin(PipelineCache.class)
public interface PipelineCacheAccessor {
    @Accessor("cache")
    Map<RenderPipeline, CompiledRenderPipeline> polydium$cache();
}
//?} else {
/*@Mixin(targets = /^? if >= 26.2 {^/{"com.mojang.renderpearl.backend.opengl.GlDevice", "com.mojang.renderpearl.backend.vulkan.VulkanDevice"}/^?} else {^//^"com.mojang.renderpearl.backend.opengl.GlDevice"^//^?}^/)
public interface PipelineCacheAccessor {
    @Accessor("pipelineCache")
    Map<RenderPipeline, ? extends CompiledRenderPipeline> polydium$cache();
}
*///?}
//?}
