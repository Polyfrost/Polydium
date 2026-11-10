package org.polyfrost.polydium.mixin.client.perf.punchy;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets = "punchy.client.state.MovementStateMachine", remap = false)
public class MixinPunchySlimeCheckCache {
    @Unique
    private static LocalPlayer polydium$slimePlayer;
    @Unique
    private static int polydium$slimeTick = Integer.MIN_VALUE;
    @Unique
    private static boolean polydium$slimeBelow;

    @WrapMethod(method = "isStandingOnSlime", remap = false, require = 0)
    private static boolean polydium$cachedSlimeCheck(LocalPlayer player, Operation<Boolean> original) {
        if (player != polydium$slimePlayer || player.tickCount != polydium$slimeTick) {
            polydium$slimeBelow = original.call(player);
            polydium$slimePlayer = player;
            polydium$slimeTick = player.tickCount;
        }
        return polydium$slimeBelow;
    }
}
