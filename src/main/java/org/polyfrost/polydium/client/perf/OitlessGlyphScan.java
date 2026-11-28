package org.polyfrost.polydium.client.perf;

//? if >= 26.3 {
import java.util.function.Predicate;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import org.polyfrost.polydium.mixin.client.perf.BakedSheetGlyphAccessor;

public final class OitlessGlyphScan implements FormattedCharSink {
    private final Font.Provider provider;
    private final Font.DisplayMode glyphMode;
    private final Font.DisplayMode textMode;
    private final Predicate<RenderType> lacksOit;
    private GlyphRenderTypes checked;
    private boolean effects;

    public OitlessGlyphScan(Font.Provider provider, Font.DisplayMode glyphMode, Font.DisplayMode textMode, Predicate<RenderType> lacksOit) {
        this.provider = provider;
        this.glyphMode = glyphMode;
        this.textMode = textMode;
        this.lacksOit = lacksOit;
    }

    public boolean hasEffects() {
        return this.effects;
    }

    @Override
    public boolean accept(int index, Style style, int codePoint) {
        if (style.isObfuscated())
            return false;
        BakedGlyph glyph = this.provider.glyphs(style.getFont()).getGlyph(codePoint);
        if (glyph instanceof BakedSheetGlyph) {
            GlyphRenderTypes types = ((BakedSheetGlyphAccessor) glyph).polydium$renderTypes();
            if (types != this.checked) {
                if (this.lacksOit.test(types.select(this.glyphMode)) || this.glyphMode != this.textMode && this.lacksOit.test(types.select(this.textMode)))
                    return false;
                this.checked = types;
            }
        } else {
            TextRenderable renderable = glyph.createGlyph(0.0F, 0.0F, -1, 0, style, 0.0F, 0.0F);
            if (renderable != null && (this.lacksOit.test(renderable.renderType(this.glyphMode)) || this.glyphMode != this.textMode && this.lacksOit.test(renderable.renderType(this.textMode))))
                return false;
        }
        if (style.isStrikethrough() || style.isUnderlined())
            this.effects = true;
        return true;
    }
}
//?}
