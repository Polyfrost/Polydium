package org.polyfrost.polydium.mixin.client.perf;

//? if < 1.21.4 {
/*import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BakedGlyph.Effect.class)
public interface BakedGlyphEffectAccessor {
    @Accessor("x0") float polydium$x0();
    @Accessor("y0") float polydium$y0();
    @Accessor("x1") float polydium$x1();
    @Accessor("y1") float polydium$y1();
    @Accessor("depth") float polydium$depth();
    @Accessor("r") float polydium$r();
    @Accessor("g") float polydium$g();
    @Accessor("b") float polydium$b();
    @Accessor("a") float polydium$a();
}
*///?}
