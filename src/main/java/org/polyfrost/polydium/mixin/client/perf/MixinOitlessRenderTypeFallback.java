package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.phase.FeatureRenderPhase;
import net.minecraft.client.renderer.feature.phase.SimpleFeatureRenderPhase;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SubmitNodeCollection.class)
public class MixinOitlessRenderTypeFallback {
    @Shadow
    @Final
    public SimpleFeatureRenderPhase solid;

    @Shadow
    @Final
    public SimpleFeatureRenderPhase oitTranslucent;

    @ModifyExpressionValue(method = "submitCustomGeometry", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/SubmitNodeCollection;translucentCustomGeometry:Lnet/minecraft/client/renderer/feature/phase/SimpleFeatureRenderPhase;"))
    private SimpleFeatureRenderPhase polydium$customGeometryWithoutOit(SimpleFeatureRenderPhase phase, @Local(argsOnly = true) RenderType renderType) {
        return this.polydium$needsFallback(phase, renderType) ? this.solid : phase;
    }

    @ModifyExpressionValue(method = "submitModel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/SubmitNodeCollection;translucentModels:Lnet/minecraft/client/renderer/feature/phase/FeatureRenderPhase;"))
    private FeatureRenderPhase<?> polydium$modelWithoutOit(FeatureRenderPhase<?> phase, @Local(argsOnly = true) RenderType renderType) {
        return this.polydium$needsFallback(phase, renderType) ? this.solid : phase;
    }

    @ModifyExpressionValue(method = "submitBlockModel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/SubmitNodeCollection;translucentBlocksAndItems:Lnet/minecraft/client/renderer/feature/phase/FeatureRenderPhase;"))
    private FeatureRenderPhase<?> polydium$blockModelWithoutOit(FeatureRenderPhase<?> phase, @Local(argsOnly = true) RenderType renderType) {
        return this.polydium$needsFallback(phase, renderType) ? this.solid : phase;
    }

    @Unique
    private boolean polydium$needsFallback(FeatureRenderPhase<?> phase, RenderType renderType) {
        return phase == this.oitTranslucent && ((RenderSetupOitAccessor) (Object) ((RenderTypeSetupAccessor) renderType).polydium$state()).polydium$oitPipelineSet() == null;
    }
}
//?}
