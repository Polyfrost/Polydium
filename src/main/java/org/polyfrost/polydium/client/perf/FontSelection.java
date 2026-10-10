package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import com.google.common.collect.Lists;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SpaceProvider;
import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import com.mojang.blaze3d.font.UnbakedGlyph;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.font.AllMissingGlyphProvider;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.glyphs.SpecialGlyphs;
import net.minecraft.client.gui.font.providers.BitmapProvider;
import net.minecraft.client.gui.font.providers.UnihexProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FontSelection {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final boolean AVAILABLE = !FabricLoader.getInstance().isModLoaded("quick-pack");

    private static final Set<Class<?>> LOOKUP_PROVIDERS = Set.of(BitmapProvider.class, UnihexProvider.class, SpaceProvider.class, AllMissingGlyphProvider.class);
    private static final Set<Class<?>> EXACT_SET_PROVIDERS = Set.of(BitmapProvider.class, UnihexProvider.class, SpaceProvider.class, TrueTypeGlyphProvider.class);

    private static volatile Pending pending;

    private FontSelection() {
    }

    public static void prepare(Map<Identifier, List<GlyphProvider.Conditional>> fontSets, Set<FontOption> options) {
        try {
            List<Entry> entries = new ArrayList<>(fontSets.size());
            Map<GlyphProvider, IntSet> supported = new IdentityHashMap<>();
            for (List<GlyphProvider.Conditional> providers : fontSets.values()) {
                List<GlyphProvider.Conditional> ordered = List.copyOf(Lists.reverse(providers));
                entries.add(new Entry(ordered, select(ordered, options, supported)));
            }
            pending = new Pending(Set.copyOf(options), entries);
        } catch (RuntimeException e) {
            pending = null;
            LOGGER.warn("Could not select font providers ahead of time, leaving it to the main thread", e);
        }
    }

    public static Selection take(List<GlyphProvider.Conditional> providers, Set<FontOption> options) {
        Pending current = pending;
        if (current == null || !current.options().equals(options)) {
            return null;
        }
        for (Entry entry : current.entries()) {
            if (sameProviders(entry.providers(), providers)) {
                return entry.selection();
            }
        }
        return null;
    }

    public static boolean onlyLookupProviders(List<GlyphProvider.Conditional> providers) {
        for (GlyphProvider.Conditional provider : providers) {
            if (!LOOKUP_PROVIDERS.contains(provider.provider().getClass())) {
                return false;
            }
        }
        return true;
    }

    public static void clear() {
        pending = null;
    }

    private static Selection select(List<GlyphProvider.Conditional> providers, Set<FontOption> options, Map<GlyphProvider, IntSet> supported) {
        Int2ObjectMap<IntList> glyphsByWidth = new Int2ObjectOpenHashMap<>();
        IntSet supportedGlyphs = new IntOpenHashSet();
        List<GlyphProvider> selectedProviders = new ArrayList<>();
        for (GlyphProvider.Conditional conditionalProvider : providers) {
            if (conditionalProvider.filter().apply(options)) {
                selectedProviders.add(conditionalProvider.provider());
                supportedGlyphs.addAll(supported.computeIfAbsent(conditionalProvider.provider(), GlyphProvider::getSupportedGlyphs));
            }
        }

        Set<GlyphProvider> usedProviders = new HashSet<>();
        Owners owners = owners(selectedProviders, supported);
        supportedGlyphs.forEach(codepoint -> {
            if (owners != null) {
                GlyphProvider owner = owners.first().getOrDefault(codepoint, owners.catchAll());
                UnbakedGlyph glyph = owner == null ? null : owner.getGlyph(codepoint);
                if (glyph != null) {
                    record(owner, glyph, codepoint, usedProviders, glyphsByWidth);
                    return;
                }
            }
            for (GlyphProvider provider : selectedProviders) {
                UnbakedGlyph glyph = provider.getGlyph(codepoint);
                if (glyph != null) {
                    record(provider, glyph, codepoint, usedProviders, glyphsByWidth);
                    break;
                }
            }
        });
        return new Selection(selectedProviders.stream().filter(usedProviders::contains).toList(), glyphsByWidth);
    }

    private static void record(GlyphProvider provider, UnbakedGlyph glyph, int codepoint, Set<GlyphProvider> usedProviders, Int2ObjectMap<IntList> glyphsByWidth) {
        usedProviders.add(provider);
        if (glyph.info() != SpecialGlyphs.MISSING) {
            glyphsByWidth.computeIfAbsent(Mth.ceil(glyph.info().getAdvance(false)), width -> new IntArrayList()).add(codepoint);
        }
    }

    private static Owners owners(List<GlyphProvider> selectedProviders, Map<GlyphProvider, IntSet> supported) {
        Int2ObjectMap<GlyphProvider> first = new Int2ObjectOpenHashMap<>();
        for (GlyphProvider provider : selectedProviders) {
            if (provider.getClass() == AllMissingGlyphProvider.class) {
                return new Owners(first, provider);
            }
            if (!EXACT_SET_PROVIDERS.contains(provider.getClass())) {
                return null;
            }
            supported.get(provider).forEach(codepoint -> first.putIfAbsent(codepoint, provider));
        }
        return new Owners(first, null);
    }

    private static boolean sameProviders(List<GlyphProvider.Conditional> expected, List<GlyphProvider.Conditional> actual) {
        if (expected.size() != actual.size()) {
            return false;
        }
        for (int i = 0; i < expected.size(); i++) {
            GlyphProvider.Conditional left = expected.get(i);
            GlyphProvider.Conditional right = actual.get(i);
            if (left.provider() != right.provider() || left.filter() != right.filter()) {
                return false;
            }
        }
        return true;
    }

    public record Selection(List<GlyphProvider> activeProviders, Int2ObjectMap<IntList> glyphsByWidth) {
    }

    private record Owners(Int2ObjectMap<GlyphProvider> first, GlyphProvider catchAll) {
    }

    private record Entry(List<GlyphProvider.Conditional> providers, Selection selection) {
    }

    private record Pending(Set<FontOption> options, List<Entry> entries) {
    }
}
//?}
