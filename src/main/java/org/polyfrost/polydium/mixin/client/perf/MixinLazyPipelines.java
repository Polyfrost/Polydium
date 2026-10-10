package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if < 26.3 {
/*import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.ShaderSource;
*///?}
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.ShaderManager;
import org.polyfrost.polydium.client.perf.LazyPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
//? if >= 26.3 {
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//?} else {
/*import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?}

import java.util.List;

@Mixin(ShaderManager.class)
public abstract class MixinLazyPipelines {
    //? if >= 26.3 {
    @Inject(method = "reload", at = @At("HEAD"))
    private void polydium$rememberLazyPipelines(CallbackInfoReturnable<?> cir) {
    //?} else {
    /*@Inject(method = "apply(Lnet/minecraft/client/renderer/ShaderManager$Configs;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
    private void polydium$rememberLazyPipelines(CallbackInfo ci) {
    *///?}
        LazyPipelines.remember();
    }

    //? if >= 26.3 {
    @WrapOperation(
        method = "lambda$reload$1",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderPipelines;optionalPipelines()Ljava/util/List;")
    )
    private static List<RenderPipeline> polydium$compileLazyPipelines(Operation<List<RenderPipeline>> original) {
        return LazyPipelines.withRemembered(original.call());
    }
    //?} else {
    /*@WrapOperation(
        method = "apply(Lnet/minecraft/client/renderer/ShaderManager$Configs;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuDevice;precompilePipeline(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lcom/mojang/renderpearl/api/pipeline/ShaderSource;)Lcom/mojang/renderpearl/api/pipeline/CompiledRenderPipeline;")
    )
    private CompiledRenderPipeline polydium$compileLazyPipelines(GpuDevice device, RenderPipeline pipeline, ShaderSource source, Operation<CompiledRenderPipeline> original) {
        for (RenderPipeline lazy : LazyPipelines.withRemembered(List.of())) {
            device.precompilePipeline(lazy, source);
        }
        return original.call(device, pipeline, source);
    }
    *///?}
}
//?}
