package org.polyfrost.polydium.mixin.client.perf.punchy;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets = "punchy.config.PunchyTuningConfig", remap = false)
public class MixinPunchyMixOptionCache {
    @Unique
    private static final long POLYDIUM$REFRESH_NANOS = 100_000_000L;
    @Unique
    private static final int POLYDIUM$SLOTS = 8;
    @Unique
    private static Set<String> polydium$activeOptions;
    @Unique
    private static long polydium$activeCheckedAt;
    @Unique
    private static final ItemStack[] polydium$stack = new ItemStack[POLYDIUM$SLOTS];
    @Unique
    private static final Item[] polydium$item = new Item[POLYDIUM$SLOTS];
    @Unique
    private static final String[] polydium$type = new String[POLYDIUM$SLOTS];
    @Unique
    private static final String[] polydium$option = new String[POLYDIUM$SLOTS];
    @Unique
    private static final long[] polydium$checkedAt = new long[POLYDIUM$SLOTS];
    @Unique
    private static int polydium$next;

    @WrapMethod(method = "getActiveMixPackOptionIds", remap = false, require = 0)
    private static Set<String> polydium$cachedActiveOptions(Operation<Set<String>> original) {
        if (!RenderSystem.isOnRenderThread()) {
            return original.call();
        }
        long now = System.nanoTime();
        Set<String> cached = polydium$activeOptions;
        if (cached != null && now - polydium$activeCheckedAt < POLYDIUM$REFRESH_NANOS) {
            return cached;
        }
        Set<String> result = original.call();
        if (result != null) {
            polydium$activeOptions = Collections.unmodifiableSet(new LinkedHashSet<>(result));
            polydium$activeCheckedAt = now;
        }
        return result;
    }

    @WrapMethod(method = "getMixPreferredOptionForType", remap = false, require = 0)
    private static String polydium$cachedOptionForType(ItemStack stack, String entryType, Operation<String> original) {
        if (stack == null || entryType == null || !RenderSystem.isOnRenderThread()) {
            return original.call(stack, entryType);
        }
        long now = System.nanoTime();
        Item item = stack.getItem();
        for (int i = 0; i < POLYDIUM$SLOTS; i++) {
            if (polydium$stack[i] == stack && polydium$item[i] == item && entryType.equals(polydium$type[i])
                && now - polydium$checkedAt[i] < POLYDIUM$REFRESH_NANOS) {
                return polydium$option[i];
            }
        }
        String result = original.call(stack, entryType);
        int slot = polydium$next;
        polydium$next = (slot + 1) % POLYDIUM$SLOTS;
        polydium$stack[slot] = stack;
        polydium$item[slot] = item;
        polydium$type[slot] = entryType;
        polydium$option[slot] = result;
        polydium$checkedAt[slot] = now;
        return result;
    }
}
