package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.font.GlyphProvider;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.FontSet;
import org.polyfrost.polydium.client.perf.FontSelection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.Set;

@Mixin(FontSet.class)
public abstract class MixinFontSelectionSet {
    @Shadow
    @Final
    private Int2ObjectMap<IntList> glyphsByWidth;

    @Unique
    private List<GlyphProvider.Conditional> polydium$selectedFrom;
    @Unique
    private Set<FontOption> polydium$selectedFor;
    @Unique
    private List<GlyphProvider> polydium$selected;
    @Unique
    private Int2ObjectMap<IntList> polydium$selectedWidths;

    @WrapMethod(method = "selectProviders")
    private List<GlyphProvider> polydium$usePreparedSelection(List<GlyphProvider.Conditional> providers, Set<FontOption> options, Operation<List<GlyphProvider>> original) {
        if (!FontSelection.AVAILABLE) {
            this.polydium$selectedFrom = null;
            return original.call(providers, options);
        }
        if (this.polydium$selectedFrom != null && polydium$sameList(providers, this.polydium$selectedFrom) && options.equals(this.polydium$selectedFor)) {
            this.glyphsByWidth.putAll(this.polydium$selectedWidths);
            return this.polydium$selected;
        }

        FontSelection.Selection selection = FontSelection.take(providers, options);
        List<GlyphProvider> selected;
        if (selection == null) {
            selected = original.call(providers, options);
        } else {
            this.glyphsByWidth.putAll(selection.glyphsByWidth());
            selected = selection.activeProviders();
        }

        this.polydium$selectedFrom = List.copyOf(providers);
        this.polydium$selectedFor = Set.copyOf(options);
        this.polydium$selected = selected;
        this.polydium$selectedWidths = new Int2ObjectOpenHashMap<>(this.glyphsByWidth);
        return selected;
    }

    @Unique
    private static boolean polydium$sameList(List<GlyphProvider.Conditional> left, List<GlyphProvider.Conditional> right) {
        if (left.size() != right.size()) {
            return false;
        }
        for (int i = 0; i < left.size(); i++) {
            if (left.get(i) != right.get(i)) {
                return false;
            }
        }
        return true;
    }
}
//?}
