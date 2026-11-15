package org.polyfrost.polydium.mixin.client.perf.lambdabettergrass;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(targets = "dev.lambdaurora.lambdabettergrass.metadata.layer.LBGLayerTypeData", remap = false)
public abstract class MixinLayerMatchLoop {
    @Shadow @Final
    private BlockState state;

    @Shadow @Final
    private List<? extends Predicate<BlockState>> matchers;

    @Overwrite
    public boolean match(BlockState state) {
        List<? extends Predicate<BlockState>> matchers = this.matchers;
        int size = matchers.size();
        if (size == 0)
            return state.is(this.state.getBlock());

        for (int i = 0; i < size; i++) {
            if (matchers.get(i).test(state))
                return true;
        }
        return false;
    }
}
