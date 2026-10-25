package org.polyfrost.polydium.mixin.client.perf.punchy;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets = "punchy.client.render.ToolTuning", remap = false)
public class MixinPunchyAnimationMergeCache {
    @Unique
    private static final int POLYDIUM$SLOTS = 8;
    @Unique
    private static final long POLYDIUM$REFRESH_NANOS = 250_000_000L;
    @Unique
    private static final List<?>[] polydium$primary = new List<?>[POLYDIUM$SLOTS];
    @Unique
    private static final List<?>[] polydium$defaults = new List<?>[POLYDIUM$SLOTS];
    @Unique
    private static final int[] polydium$primarySize = new int[POLYDIUM$SLOTS];
    @Unique
    private static final int[] polydium$defaultsSize = new int[POLYDIUM$SLOTS];
    @Unique
    private static final List<?>[] polydium$result = new List<?>[POLYDIUM$SLOTS];
    @Unique
    private static final long[] polydium$checkedAt = new long[POLYDIUM$SLOTS];
    @Unique
    private static int polydium$next;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @WrapMethod(method = "mergeAnimationEntries", remap = false, require = 0)
    private static List polydium$cachedMerge(List primary, List defaults, Operation<List> original) {
        if (primary == null || primary.isEmpty() || defaults == null || defaults.isEmpty() || !RenderSystem.isOnRenderThread()) {
            return original.call(primary, defaults);
        }
        long now = System.nanoTime();
        int primarySize = primary.size();
        int defaultsSize = defaults.size();
        for (int i = 0; i < POLYDIUM$SLOTS; i++) {
            if (polydium$primary[i] == primary && polydium$defaults[i] == defaults
                && polydium$primarySize[i] == primarySize && polydium$defaultsSize[i] == defaultsSize
                && now - polydium$checkedAt[i] < POLYDIUM$REFRESH_NANOS) {
                return polydium$result[i];
            }
        }
        List result = original.call(primary, defaults);
        int slot = polydium$next;
        polydium$next = (slot + 1) % POLYDIUM$SLOTS;
        polydium$primary[slot] = primary;
        polydium$defaults[slot] = defaults;
        polydium$primarySize[slot] = primarySize;
        polydium$defaultsSize[slot] = defaultsSize;
        polydium$result[slot] = result;
        polydium$checkedAt[slot] = now;
        return result;
    }
}
