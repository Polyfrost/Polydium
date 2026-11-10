package org.polyfrost.polydium.mixin.client.perf.punchy;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "punchy.config.PunchyConfig", remap = false)
public class MixinPunchyBlacklistCache {
    @Unique
    private static final String POLYDIUM$NO_MATCH = new String("");
    @Unique
    private static final Reference2ObjectOpenHashMap<Item, String> polydium$matches = new Reference2ObjectOpenHashMap<>();

    @WrapMethod(method = "findBlacklistMatch", remap = false, require = 0)
    private static String polydium$cachedBlacklistMatch(ItemStack stack, Operation<String> original) {
        if (stack == null || stack.isEmpty() || !RenderSystem.isOnRenderThread()) return original.call(stack);
        Item item = stack.getItem();
        String match = polydium$matches.get(item);
        if (match == null) {
            match = original.call(stack);
            polydium$matches.put(item, match == null ? POLYDIUM$NO_MATCH : match);
        }
        return match == POLYDIUM$NO_MATCH ? null : match;
    }

    @Inject(method = {"load", "applyAndSave"}, at = @At("RETURN"), remap = false, require = 0)
    private static void polydium$clearBlacklistCache(CallbackInfo ci) {
        polydium$matches.clear();
    }
}
