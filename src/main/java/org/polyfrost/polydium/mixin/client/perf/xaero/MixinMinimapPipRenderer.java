package org.polyfrost.polydium.mixin.client.perf.xaero;

//? if xaerominimap && >= 1.21.6 {
//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.injection.At;
import xaero.common.core.IPictureInPictureRenderer;
import xaero.hud.minimap.render.MinimapPipRenderer;
//?}
//? if >= 26.1 {
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
//?} else {
/*import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
*///?}
//? if < 26.2
//import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import org.polyfrost.polydium.compat.XaeroMinimapHudCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.render.MinimapPipRenderState;

import java.util.Objects;

@Pseudo
@Mixin(targets = "xaero.hud.minimap.render.MinimapPipRenderer", remap = false)
public abstract class MixinMinimapPipRenderer extends PictureInPictureRenderer<PictureInPictureRenderState> {
    @Unique
    private final XaeroMinimapHudCache polydium$hudCache = new XaeroMinimapHudCache();

    //? if < 26.2 {
    /*protected MixinMinimapPipRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }
    *///?}

    @Override
    protected boolean textureIsReadyToBlit(PictureInPictureRenderState renderState) {
        return polydium$reuse((MinimapPipRenderState) renderState);
    }

    @Unique
    private boolean polydium$reuse(MinimapPipRenderState state) {
        int key = Objects.hash(state.getWidth(), state.getHeight(), state.getScale(), state.getMinimapScale(), state.getSize(), state.getBoxSize());
        if (!polydium$hudCache.reuse(key)) return false;
        MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
        if (session != null) session.getProcessor().updateZoom();
        return true;
    }

    //? if >= 26.3 {
    private static final String PREPARE = "prepare(Lxaero/hud/minimap/render/MinimapPipRenderState;Lnet/minecraft/client/renderer/state/gui/GuiRenderState;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;I)V";

    @Unique
    private boolean polydium$reused;

    @WrapWithCondition(method = PREPARE, at = @At(value = "INVOKE", target = "Lxaero/common/core/IPictureInPictureRenderer;xaero_mm_prepareTexturesAndProjection(ZII)V"), remap = false)
    private boolean polydium$skipClear(IPictureInPictureRenderer renderer, boolean needsResize, int width, int height, @Local(argsOnly = true) MinimapPipRenderState state) {
        polydium$reused = !needsResize && polydium$reuse(state);
        return !polydium$reused;
    }

    @WrapWithCondition(method = PREPARE, at = @At(value = "INVOKE", target = "Lxaero/hud/minimap/render/MinimapPipRenderer;renderToTexture(Lxaero/hud/minimap/render/MinimapPipRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"), remap = false)
    private boolean polydium$skipRender(MinimapPipRenderer<?> renderer, MinimapPipRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        return !polydium$reused;
    }
    //?}
}
//?}
