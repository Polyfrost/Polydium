package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.polyfrost.polydium.client.perf.OutlineScissor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(targets = "net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer$Group")
public abstract class MixinOutlineScissorGroup implements OutlineScissor.DrawGroup {
    @Shadow
    @Final
    private List<StagedVertexBuffer.Draw> draws;

    @Shadow
    @Final
    private List<PreparedRenderType> drawRenderTypes;

    @Override
    public List<StagedVertexBuffer.Draw> polydium$draws() {
        return this.draws;
    }

    @Override
    public List<PreparedRenderType> polydium$drawRenderTypes() {
        return this.drawRenderTypes;
    }

    @ModifyExpressionValue(method = "getOrAddDraw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/StagedVertexBuffer;appendDraw(Lcom/mojang/renderpearl/api/vertex/VertexFormat;Lcom/mojang/renderpearl/api/pipeline/PrimitiveTopology;Lcom/mojang/blaze3d/vertex/VertexSorting;)Lnet/minecraft/client/renderer/StagedVertexBuffer$Draw;"))
    private StagedVertexBuffer.Draw polydium$trackOutlineDraw(StagedVertexBuffer.Draw draw, @Local(argsOnly = true) RenderType renderType) {
        if (PolydiumToggles.outlineScissor && renderType.isOutline() && renderType.format() == DefaultVertexFormat.POSITION_TEX_COLOR)
            ((OutlineScissor.Draw) draw).polydium$trackOutline();
        return draw;
    }
}
//?}
