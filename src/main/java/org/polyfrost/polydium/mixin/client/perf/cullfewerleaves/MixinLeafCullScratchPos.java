package org.polyfrost.polydium.mixin.client.perf.cullfewerleaves;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "dev.microcontrollers.cullfewerleaves.CullFewerLeaves", remap = false)
public abstract class MixinLeafCullScratchPos {
    @Unique
    private static final ThreadLocal<BlockPos.MutableBlockPos[]> polydium$scratch =
            ThreadLocal.withInitial(() -> new BlockPos.MutableBlockPos[]{new BlockPos.MutableBlockPos(), new BlockPos.MutableBlockPos()});

    @Redirect(method = "shouldCullSide", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Vec3i;multiply(I)Lnet/minecraft/core/Vec3i;", remap = true), remap = false, require = 0)
    private static Vec3i polydium$scaledNormal(Vec3i normal, int scale) {
        if (scale == 1) {
            return normal.multiply(scale);
        }
        return polydium$scratch.get()[0].set(normal.getX() * scale, normal.getY() * scale, normal.getZ() * scale);
    }

    @Redirect(method = "shouldCullSide", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;offset(Lnet/minecraft/core/Vec3i;)Lnet/minecraft/core/BlockPos;", remap = true), remap = false, require = 0)
    private static BlockPos polydium$neighbor(BlockPos pos, Vec3i offset) {
        return polydium$scratch.get()[1].setWithOffset(pos, offset);
    }
}
