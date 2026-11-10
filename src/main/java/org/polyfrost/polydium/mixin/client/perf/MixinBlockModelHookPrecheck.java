package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.1 {
import net.minecraft.client.renderer.block.BlockStateModelSet;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = BlockStateModelSet.class, priority = 1100)
public abstract class MixinBlockModelHookPrecheck {
}
//?}
