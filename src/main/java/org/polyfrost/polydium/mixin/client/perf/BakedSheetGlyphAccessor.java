package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BakedSheetGlyph.class)
public interface BakedSheetGlyphAccessor {
    @Accessor("renderTypes")
    GlyphRenderTypes polydium$renderTypes();
}
//?}
