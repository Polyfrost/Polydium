package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunkSection.class)
public abstract class MixinSectionAirCount {
    @Shadow
    private short nonEmptyBlockCount;

    @Shadow
    @Final
    private PalettedContainer<BlockState> states;

    @Shadow
    public abstract void recalcBlockCounts();

    @Inject(method = "read", at = @At("TAIL"))
    private void polydium$recountEmptyClaim(FriendlyByteBuf buffer, CallbackInfo ci) {
        if (this.nonEmptyBlockCount == 0 && this.states.maybeHas(state -> !state.isAir()))
            this.recalcBlockCounts();
    }
}
