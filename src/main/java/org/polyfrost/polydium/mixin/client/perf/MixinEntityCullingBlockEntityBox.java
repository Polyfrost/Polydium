package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.polyfrost.polydium.client.perf.BlockEntityCullBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Entity Culling builds two AABBs per block entity per frame for its frustum test
@Mixin(value = BlockEntityRenderDispatcher.class, priority = 1500)
public abstract class MixinEntityCullingBlockEntityBox {
    private static final String POLYDIUM$MIXIN = "dev.tr7zw.entityculling.mixin.BlockEntityRenderDispatcherMixin";

    @Shadow
    public abstract <E extends BlockEntity, S extends BlockEntityRenderState> BlockEntityRenderer<E, S> getRenderer(E blockEntity);

    @Unique
    private int polydium$rendererEpoch = 1;
    @Unique
    private BlockEntity polydium$cullTarget;

    @Inject(method = "onResourceManagerReload", at = @At("RETURN"))
    private void polydium$invalidateCullBoxes(CallbackInfo ci) {
        this.polydium$rendererEpoch++;
    }

    @TargetHandler(mixin = POLYDIUM$MIXIN, name = "tryExtractRenderState")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Ldev/tr7zw/entityculling/EntityCullingModBase;setupAABB(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/AABB;"), require = 0, expect = 0)
    private AABB polydium$inline$cachedBox(@Coerce Object mod, BlockEntity blockEntity, BlockPos pos, Operation<AABB> original) {
        this.polydium$cullTarget = blockEntity;
        AABB cached = ((BlockEntityCullBox) blockEntity).polydium$cullBox(this.polydium$rendererEpoch);
        return cached != null ? cached : original.call(mod, blockEntity, pos);
    }

    @TargetHandler(mixin = POLYDIUM$MIXIN, name = "tryExtractRenderState")
    @Redirect(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(D)Lnet/minecraft/world/phys/AABB;"), require = 0, expect = 0)
    private AABB polydium$cachedInflate(AABB box, double amount) {
        BlockEntity blockEntity = this.polydium$cullTarget;
        this.polydium$cullTarget = null;
        BlockEntityCullBox holder = (BlockEntityCullBox) blockEntity;
        if (holder.polydium$cullBox(this.polydium$rendererEpoch) == box) {
            return box;
        }
        AABB inflated = box.inflate(amount);
        if (holder.polydium$cullBoxEpoch() != this.polydium$rendererEpoch) {
            boolean fixed = BlockEntityCullBox.fixedBounds(this.getRenderer(blockEntity).getClass());
            holder.polydium$setCullBox(fixed ? inflated : null, this.polydium$rendererEpoch);
        }
        return inflated;
    }
}
//?}
