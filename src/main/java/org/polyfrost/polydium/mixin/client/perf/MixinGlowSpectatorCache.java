package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.polyfrost.polydium.client.perf.PerfClock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public class MixinGlowSpectatorCache {
    @Unique
    private long polydium$spectatorFrame = -1L;
    @Unique
    private LocalPlayer polydium$spectatorPlayer;
    @Unique
    private boolean polydium$spectator;

    @WrapOperation(
        method = "shouldEntityAppearGlowing",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z")
    )
    private boolean polydium$spectatorOncePerFrame(LocalPlayer player, Operation<Boolean> original) {
        if (!((Minecraft) (Object) this).isSameThread()) {
            return original.call(player);
        }
        long frame = PerfClock.frame;
        if (frame == this.polydium$spectatorFrame && player == this.polydium$spectatorPlayer) {
            return this.polydium$spectator;
        }
        boolean spectator = original.call(player);
        this.polydium$spectatorFrame = frame;
        this.polydium$spectatorPlayer = player;
        this.polydium$spectator = spectator;
        return spectator;
    }
}
//?}
