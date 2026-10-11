package org.polyfrost.polydium.mixin.client.perf;

//? if < 26.1 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientLevel.class)
public abstract class MixinAnimateTickRandom {
    @WrapOperation(
            method = "animateTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;create()Lnet/minecraft/util/RandomSource;")
    )
    private RandomSource polydium$singleThreadedRandom(Operation<RandomSource> original) {
        return RandomSource.createNewThreadLocalInstance();
    }
}
*///?}
