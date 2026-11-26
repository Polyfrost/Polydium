package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.TextFeatureRenderer;
import net.minecraft.client.renderer.feature.phase.FeatureRenderPhase;
import net.minecraft.client.renderer.feature.phase.SimpleFeatureRenderPhase;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import org.polyfrost.polydium.client.perf.OitlessRenderTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SubmitNodeCollection.class)
public class MixinOitlessRenderTypeFallback {
    @Unique
    private static final OitlessRenderTypes<RenderType> POLYDIUM_OITLESS_TEXT = new OitlessRenderTypes<>(MixinOitlessRenderTypeFallback::polydium$lacksOitPipeline);

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
        Font.Provider provider = ((FontProviderAccessor) Minecraft.getInstance().font).polydium$provider();
        return switch (content) {
            case TextFeatureRenderer.Content.StandaloneBackground ignored -> polydium$effectLacksOitPipeline(provider, displayMode);
            case TextFeatureRenderer.Content.Text text -> text.outlineColor() == 0
                    ? polydium$glyphsLackOitPipeline(provider, text.string(), displayMode, displayMode)
                    || ARGB.alpha(text.backgroundColor()) != 0 && polydium$effectLacksOitPipeline(provider, displayMode)
                    : polydium$glyphsLackOitPipeline(provider, text.string(), Font.DisplayMode.NORMAL, Font.DisplayMode.POLYGON_OFFSET);
        };
    }

    @Unique
    private static boolean polydium$glyphsLackOitPipeline(Font.Provider provider, FormattedCharSequence string, Font.DisplayMode glyphMode, Font.DisplayMode textMode) {
        return !string.accept((index, style, codePoint) -> {
            if (style.isObfuscated())
                return false;
            BakedGlyph glyph = provider.glyphs(style.getFont()).getGlyph(codePoint);
            if (polydium$glyphLacksOitPipeline(glyph, style, glyphMode) || glyphMode != textMode && polydium$glyphLacksOitPipeline(glyph, style, textMode))
                return false;
            return !(style.isStrikethrough() || style.isUnderlined())
                    || !polydium$effectLacksOitPipeline(provider, textMode) && (glyphMode == textMode || !polydium$effectLacksOitPipeline(provider, glyphMode));
        });
    }

    @Unique
    private static boolean polydium$glyphLacksOitPipeline(BakedGlyph glyph, Style style, Font.DisplayMode displayMode) {
        if (glyph instanceof BakedSheetGlyph)
            return polydium$cachedLacksOitPipeline(((BakedSheetGlyphAccessor) glyph).polydium$renderTypes().select(displayMode));
        TextRenderable renderable = glyph.createGlyph(0.0F, 0.0F, -1, 0, style, 0.0F, 0.0F);
        return renderable != null && polydium$cachedLacksOitPipeline(renderable.renderType(displayMode));
    }

    @Unique
    private static boolean polydium$effectLacksOitPipeline(Font.Provider provider, Font.DisplayMode displayMode) {
        EffectGlyph effect = provider.effect();
        if (effect instanceof BakedSheetGlyph)
            return polydium$cachedLacksOitPipeline(((BakedSheetGlyphAccessor) effect).polydium$renderTypes().select(displayMode));
        return polydium$cachedLacksOitPipeline(effect.createEffect(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1, 0, 0.0F).renderType(displayMode));
    }

    @Unique
    private static boolean polydium$cachedLacksOitPipeline(RenderType renderType) {
        return RenderSystem.isOnRenderThread() ? POLYDIUM_OITLESS_TEXT.lacksOit(renderType) : polydium$lacksOitPipeline(renderType);
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
