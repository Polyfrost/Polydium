package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.polyfrost.polydium.client.perf.BlockEntityLight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BrightnessCombiner.class)
public class MixinBlockEntityLightCacheCombiner {
    @ModifyReturnValue(
            method = "acceptDouble(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/level/block/entity/BlockEntity;)Lit/unimi/dsi/fastutil/ints/Int2IntFunction;",
            at = @At("RETURN"),
            require = 0
    )
    private Int2IntFunction polydium$cachedDouble(Int2IntFunction original, @Local(argsOnly = true, ordinal = 0) BlockEntity first, @Local(argsOnly = true, ordinal = 1) BlockEntity second) {
        return light -> {
            int a = BlockEntityLight.cached(first);
            int b = BlockEntityLight.cached(second);
            if (a == -1 || b == -1) {
                return original.get(light);
            }
            return Math.max(a & 0xF0, b & 0xF0) | Math.max(a & 0xF00000, b & 0xF00000);
        };
    }
}
