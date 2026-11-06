package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.polyfrost.polydium.client.perf.BlockEntityCullBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

// Entity Culling builds two AABBs per block entity per frame for its frustum test
@Mixin(value = BlockEntityRenderDispatcher.class, priority = 1500)
public abstract class MixinEntityCullingBlockEntityBox {
    private static final String POLYDIUM$MIXIN = "dev.tr7zw.entityculling.mixin.BlockEntityRenderDispatcherMixin";

    @Shadow
    public abstract <E extends BlockEntity, S extends BlockEntityRenderState> BlockEntityRenderer<E, S> getRenderer(E blockEntity);

    @TargetHandler(mixin = POLYDIUM$MIXIN, name = "tryExtractRenderState")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Ldev/tr7zw/entityculling/EntityCullingModBase;setupAABB(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/AABB;"), require = 0, expect = 0)
    private AABB polydium$inline$cachedBox(@Coerce Object mod, BlockEntity blockEntity, BlockPos pos, Operation<AABB> original) {
        AABB cached = ((BlockEntityCullBox) blockEntity).polydium$cullBox();
        return cached != null ? cached : original.call(mod, blockEntity, pos);
    }

    @TargetHandler(mixin = POLYDIUM$MIXIN, name = "tryExtractRenderState")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(D)Lnet/minecraft/world/phys/AABB;"), require = 0, expect = 0)
    private AABB polydium$inline$cachedInflate(AABB box, double amount, Operation<AABB> original, @Local(argsOnly = true) BlockEntity blockEntity) {
        BlockEntityCullBox holder = (BlockEntityCullBox) blockEntity;
        if (holder.polydium$cullBox() == box) {
            return box;
        }
        AABB inflated = original.call(box, amount);
        if (BlockEntityCullBox.fixedBounds(this.getRenderer(blockEntity).getClass())) {
            holder.polydium$setCullBox(inflated);
        }
        return inflated;
    }
}
//?}
