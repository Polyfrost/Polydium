package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.mojang.blaze3d.font.GlyphProvider;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(targets = "net.minecraft.client.gui.font.FontManager$Preparation")
public interface FontPreparationAccessor {
    @Accessor("fontSets")
    Map<Identifier, List<GlyphProvider.Conditional>> polydium$fontSets();
}
//?}
