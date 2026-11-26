package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.TextFeatureRenderer;
import net.minecraft.client.renderer.feature.phase.FeatureRenderPhase;
import net.minecraft.client.renderer.feature.phase.SimpleFeatureRenderPhase;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.ItemQuads;
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

    @ModifyExpressionValue(method = "submitShapeOutline", at = {
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/SubmitNodeCollection;shapeOutlines:Lnet/minecraft/client/renderer/feature/phase/SimpleFeatureRenderPhase;"),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/SubmitNodeCollection;afterTerrain:Lnet/minecraft/client/renderer/feature/phase/SimpleFeatureRenderPhase;")
    })
    private SimpleFeatureRenderPhase polydium$shapeOutlineWithoutOit(SimpleFeatureRenderPhase phase, @Local(argsOnly = true) RenderType renderType) {
        return this.polydium$needsFallback(phase, renderType) ? this.solid : phase;
    }

    @ModifyExpressionValue(method = "submitItem", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/SubmitNodeCollection;translucentBlocksAndItems:Lnet/minecraft/client/renderer/feature/phase/FeatureRenderPhase;"))
    private FeatureRenderPhase<?> polydium$itemWithoutOit(FeatureRenderPhase<?> phase, @Local(argsOnly = true) ItemQuads quads, @Local(argsOnly = true) FoilType foilType) {
        if (phase != this.oitTranslucent)
            return phase;

        for (BakedQuad quad : quads.translucent()) {
            BakedQuad.MaterialInfo info = quad.materialInfo();
            RenderType renderType = switch (foilType) {
                case NONE -> info.itemRenderType();
                case STANDARD -> info.itemGlintRenderType();
                case SPECIAL -> info.itemGlintSpecialRenderType();
            };
            if (polydium$lacksOitPipeline(renderType))
                return this.solid;
        }

        return phase;
    }

    @ModifyExpressionValue(method = "submitTextPart", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/SubmitNodeCollection;texts:Lnet/minecraft/client/renderer/feature/phase/SimpleFeatureRenderPhase;"))
    private SimpleFeatureRenderPhase polydium$textWithoutOit(SimpleFeatureRenderPhase phase, @Local(argsOnly = true) Font.DisplayMode displayMode, @Local(argsOnly = true) TextFeatureRenderer.Content content) {
        return phase == this.oitTranslucent && polydium$textLacksOitPipeline(displayMode, content) ? this.solid : phase;
    }

    @ModifyExpressionValue(method = "submitNameTagPart", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/SubmitNodeCollection;nameTags:Lnet/minecraft/client/renderer/feature/phase/SimpleFeatureRenderPhase;"))
    private SimpleFeatureRenderPhase polydium$nameTagWithoutOit(SimpleFeatureRenderPhase phase, @Local(argsOnly = true) TextFeatureRenderer.Submit nameTag) {
        return phase == this.oitTranslucent && polydium$textLacksOitPipeline(nameTag.displayMode(), nameTag.content()) ? this.solid : phase;
    }

    @Unique
    private static boolean polydium$textLacksOitPipeline(Font.DisplayMode displayMode, TextFeatureRenderer.Content content) {
        Font font = Minecraft.getInstance().font;
        return switch (content) {
            case TextFeatureRenderer.Content.StandaloneBackground background ->
                    polydium$lacksOitPipeline(font.prepareBackground(background.x0(), background.y0(), background.x1(), background.y1(), background.color()).renderType(displayMode));
            case TextFeatureRenderer.Content.Text text -> !text.string().accept((index, style, codePoint) -> !style.isObfuscated())
                    || (text.outlineColor() == 0
                    ? polydium$glyphsLackOitPipeline(font.prepareText(text.string(), text.x(), text.y(), text.color(), text.dropShadow(), false, text.backgroundColor()), displayMode)
                    : polydium$glyphsLackOitPipeline(font.prepare8xTextOutline(text.string(), text.x(), text.y(), text.outlineColor()), Font.DisplayMode.NORMAL)
                    || polydium$glyphsLackOitPipeline(font.prepareText(text.string(), text.x(), text.y(), text.color(), false, false, 0), Font.DisplayMode.POLYGON_OFFSET));
        };
    }

    @Unique
    private static boolean polydium$glyphsLackOitPipeline(Font.PreparedText text, Font.DisplayMode displayMode) {
        boolean[] lacking = new boolean[1];
        text.visit(new Font.GlyphVisitor() {
            @Override
            public void acceptRenderable(TextRenderable renderable) {
                if (polydium$lacksOitPipeline(renderable.renderType(displayMode)))
                    lacking[0] = true;
            }
        });
        return lacking[0];
    }

    @Unique
    private boolean polydium$needsFallback(FeatureRenderPhase<?> phase, RenderType renderType) {
        return phase == this.oitTranslucent && polydium$lacksOitPipeline(renderType);
    }

    @Unique
    private static boolean polydium$lacksOitPipeline(RenderType renderType) {
        return ((RenderSetupOitAccessor) (Object) ((RenderTypeSetupAccessor) renderType).polydium$state()).polydium$oitPipelineSet() == null;
    }
}
//?}
