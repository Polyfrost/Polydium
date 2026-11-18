package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.1 {
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelSet;
import net.minecraft.world.level.block.state.BlockState;
import org.polyfrost.polydium.client.perf.FrameModels;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockModelSet.class)
public abstract class MixinBlockModelSetRenderCaches implements FrameModels.Owner {
    @Unique
    private final FrameModels polydium$frameModels = new FrameModels();
    @Unique
    private Map<BlockState, BlockModelRenderState> polydium$displayModels;

    @Override
    public FrameModels polydium$frameModels() {
        return this.polydium$frameModels;
    }

    @Override
    public Map<BlockState, BlockModelRenderState> polydium$displayModels() {
        if (this.polydium$displayModels == null) {
            this.polydium$displayModels = new IdentityHashMap<>();
        }
        return this.polydium$displayModels;
    }
}
//?}
