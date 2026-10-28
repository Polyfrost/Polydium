package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.polyfrost.polydium.client.perf.VertexCopy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRendererVertexCopy {
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;prepareFrame(Lnet/minecraft/client/renderer/SubmitNodeStorage;)Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;"))
    private FeatureRenderDispatcher.PreparedFrame polydium$copyFeatureVerticesInBackground(FeatureRenderDispatcher dispatcher, SubmitNodeStorage storage, Operation<FeatureRenderDispatcher.PreparedFrame> original) {
        if (!PolydiumToggles.backgroundVertexCopy)
            return original.call(dispatcher, storage);

        VertexCopy.beginLevelPrepare();

        try {
            return original.call(dispatcher, storage);
        } finally {
            VertexCopy.endLevelPrepare();
        }
    }
}
//?}
