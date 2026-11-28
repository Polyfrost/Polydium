package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import org.polyfrost.polydium.client.perf.OitlessGlyphScan;
import org.polyfrost.polydium.client.perf.OitlessRenderTypes;
import org.polyfrost.polydium.client.perf.OitlessTextCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SubmitNodeCollection.class)
public class MixinOitlessRenderTypeFallback {
    @Unique
    private static final OitlessRenderTypes<RenderType> POLYDIUM_OITLESS_TEXT = new OitlessRenderTypes<>(MixinOitlessRenderTypeFallback::polydium$lacksOitPipeline);

    @Unique
    private static final OitlessTextCache<FormattedCharSequence> POLYDIUM_OITLESS_STRINGS = new OitlessTextCache<>(4096);

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
        boolean cached = RenderSystem.isOnRenderThread();
        return switch (content) {
            case TextFeatureRenderer.Content.StandaloneBackground ignored -> polydium$effectLacksOitPipeline(provider, displayMode, cached);
            case TextFeatureRenderer.Content.Text text -> text.outlineColor() == 0
                    ? polydium$cachedGlyphsLackOitPipeline(provider, text.string(), displayMode, displayMode, cached)
                    || ARGB.alpha(text.backgroundColor()) != 0 && polydium$effectLacksOitPipeline(provider, displayMode, cached)
                    : polydium$cachedGlyphsLackOitPipeline(provider, text.string(), Font.DisplayMode.NORMAL, Font.DisplayMode.POLYGON_OFFSET, cached);
        };
    }

    @Unique
    private static boolean polydium$cachedGlyphsLackOitPipeline(Font.Provider provider, FormattedCharSequence string, Font.DisplayMode glyphMode, Font.DisplayMode textMode, boolean cached) {
        if (!cached)
            return polydium$glyphsLackOitPipeline(provider, string, glyphMode, textMode, false);
        int variant = glyphMode == textMode ? textMode.ordinal() : Font.DisplayMode.values().length;
        return POLYDIUM_OITLESS_STRINGS.lacksOit(string, variant, text -> polydium$glyphsLackOitPipeline(provider, text, glyphMode, textMode, true));
    }

    @Unique
    private static boolean polydium$glyphsLackOitPipeline(Font.Provider provider, FormattedCharSequence string, Font.DisplayMode glyphMode, Font.DisplayMode textMode, boolean cached) {
        OitlessGlyphScan scan = new OitlessGlyphScan(provider, glyphMode, textMode, renderType -> polydium$lacksOitPipeline(renderType, cached));
        if (!string.accept(scan))
            return true;
        return scan.hasEffects() && (polydium$effectLacksOitPipeline(provider, textMode, cached) || glyphMode != textMode && polydium$effectLacksOitPipeline(provider, glyphMode, cached));
    }

    @Unique
    private static boolean polydium$effectLacksOitPipeline(Font.Provider provider, Font.DisplayMode displayMode, boolean cached) {
        EffectGlyph effect = provider.effect();
        if (effect instanceof BakedSheetGlyph)
            return polydium$lacksOitPipeline(((BakedSheetGlyphAccessor) effect).polydium$renderTypes().select(displayMode), cached);
        return polydium$lacksOitPipeline(effect.createEffect(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1, 0, 0.0F).renderType(displayMode), cached);
    }

    @Unique
    private static boolean polydium$lacksOitPipeline(RenderType renderType, boolean cached) {
        return cached ? POLYDIUM_OITLESS_TEXT.lacksOit(renderType) : polydium$lacksOitPipeline(renderType);
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
