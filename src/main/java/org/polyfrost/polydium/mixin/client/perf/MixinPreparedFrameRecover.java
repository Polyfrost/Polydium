package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.feature.FeatureRenderer;
import net.minecraft.client.renderer.feature.FeatureRendererMap;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FeatureRenderDispatcher.class)
public abstract class MixinPreparedFrameRecover {
    @Shadow
    @Final
    private FeatureRenderDispatcher.PreparedFrame preparedFrame;

    @Shadow
    @Final
    private StagedVertexBuffer stagedVertexBuffer;

    @Shadow
    @Final
    private FeatureRendererMap featureRenderers;

    @Inject(method = "prepareFrameWithContext", at = @At("HEAD"))
    private void polydium$closeAbandonedFrame(FeatureFrameContext context, SubmitNodeStorage storage, CallbackInfoReturnable<FeatureRenderDispatcher.PreparedFrame> cir) {
        PreparedFrameAccessor frame = (PreparedFrameAccessor) (Object) this.preparedFrame;
        if (frame.polydium$context() != null) {
            LoggerFactory.getLogger("polydium/perf").warn("Closing a feature frame left open by an earlier render error");
            try {
                this.preparedFrame.close();
            } catch (RuntimeException e) {
                LoggerFactory.getLogger("polydium/perf").warn("Failed to fully close the abandoned feature frame", e);
            }
        }
        StagedVertexBufferAccessor staged = (StagedVertexBufferAccessor) this.stagedVertexBuffer;
        if (!frame.polydium$allSubmits().isEmpty() || !staged.polydium$draws().isEmpty()) {
            for (FeatureRenderer<?> renderer : this.featureRenderers.values()) {
                try {
                    renderer.finishExecute(context);
                } catch (RuntimeException e) {
                    LoggerFactory.getLogger("polydium/perf").warn("Failed to reset feature renderer {}", renderer, e);
                }
            }
            this.stagedVertexBuffer.endDraw();
            frame.polydium$allSubmits().clear();
            frame.polydium$groupsByPhase().clear();
            frame.polydium$groupsByFeature().clear();
        }

        ByteBufferBuilder staging = staged.polydium$stagingBuffer();
        if (staged.polydium$lastVertexBuilder() != null) {
            staged.polydium$setLastVertexBuilder(null);
            staged.polydium$setLastBuildingDraw(null);
            ByteBufferBuilder.Result partial = staging.build();
            if (partial != null)
                partial.close();
        }
        staging.discard();
    }
}
//?}
