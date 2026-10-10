package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.SkyRenderer;
import org.polyfrost.polydium.client.perf.SkyFarPlane;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkyRenderer.class)
public abstract class MixinSkyFarPlane {
    @ModifyExpressionValue(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderPipelines;SKY:Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;"))
    private RenderPipeline polydium$farSky(RenderPipeline pipeline) {
        return SkyFarPlane.isDrawing() ? SkyFarPlane.Pipelines.SKY : pipeline;
    }

    @ModifyExpressionValue(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderPipelines;SUNRISE_SUNSET:Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;"))
    private RenderPipeline polydium$farSunrise(RenderPipeline pipeline) {
        return SkyFarPlane.isDrawing() ? SkyFarPlane.Pipelines.SUNRISE_SUNSET : pipeline;
    }

    @ModifyExpressionValue(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderPipelines;STARS:Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;"))
    private RenderPipeline polydium$farStars(RenderPipeline pipeline) {
        return SkyFarPlane.isDrawing() ? SkyFarPlane.Pipelines.STARS : pipeline;
    }

    @ModifyExpressionValue(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderPipelines;CELESTIAL:Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;"))
    private RenderPipeline polydium$farCelestial(RenderPipeline pipeline) {
        return SkyFarPlane.isDrawing() ? SkyFarPlane.Pipelines.CELESTIAL : pipeline;
    }
}
//?}
