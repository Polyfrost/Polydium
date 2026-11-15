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

/**
 * The client trusts the block count a server writes in front of each chunk section, and both vanilla and Sodium skip
 * meshing any section whose count is zero. Servers that build chunk packets themselves (arena instancing, spectator
 * cameras, protocol translators) sometimes send zero for sections that hold blocks, which leaves sky-coloured holes
 * until the chunk is resent. A zero count with a palette that still names a non-air state is recounted from the data.
 */
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
