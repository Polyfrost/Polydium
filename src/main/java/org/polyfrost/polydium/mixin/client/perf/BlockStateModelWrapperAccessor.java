package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.1 {
import java.util.List;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.model.BlockStateModelWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockStateModelWrapper.class)
public interface BlockStateModelWrapperAccessor {
    @Accessor("model")
    BlockStateModel polydium$model();

    @Accessor("tints")
    List<BlockTintSource> polydium$tints();
}
//?}
